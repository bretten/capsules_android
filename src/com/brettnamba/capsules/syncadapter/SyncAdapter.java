package com.brettnamba.capsules.syncadapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.json.JSONException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.annotation.TargetApi;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.brettnamba.capsules.Constants;
import com.brettnamba.capsules.dataaccess.Capsule;
import com.brettnamba.capsules.dataaccess.CapsuleOwnershipPojo;
import com.brettnamba.capsules.http.HttpFactory;
import com.brettnamba.capsules.http.RequestContract;
import com.brettnamba.capsules.http.RequestHandler;
import com.brettnamba.capsules.provider.CapsuleContract;
import com.brettnamba.capsules.provider.CapsuleOperations;
import com.brettnamba.capsules.util.JSONParser;

/**
 * SyncAdapter handles tapping into the Android framework.
 * 
 * The process of one "sync" to the server is encapsulated in onPerformSync().
 * 
 * @author Brett Namba
 *
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    /**
     * Reference to the AccountManager from the Context.
     */
    private final AccountManager mAccountManager;

    /**
     * Reference to the Context.
     */
    private final Context mContext;

    /**
     * Reference to the HTTP client.
     */
    private final HttpClient mHttpClient;

    /**
     * Reference to the HTTP request handler.
     */
    private final RequestHandler mHttpHandler;

    /**
     * Reference to the LocationManager.
     */
    private final LocationManager mLocationManager;

    /**
     * Reference to the LocationListener.
     */
    private final LocationListener mLocationListener;

    /**
     * The tag used for logging.
     */
    private static final String TAG = "SyncAdapter";

    /**
     * Constructor for Android 3.0 and later platforms
     * 
     * @param Context context
     * @param boolean autoInitialize
     * @param boolean allowParallelSyncs
     * @param LocationManager locationManager
     * @param LocationListener locationListener
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs,
            LocationManager locationManager, LocationListener locationListener) {
        super(context, autoInitialize, allowParallelSyncs);
        mContext = context;
        mAccountManager = AccountManager.get(context);

        // Get the HTTP client
        mHttpClient = HttpFactory.getInstance();
        // Get the HTTP request handler
        mHttpHandler = new RequestHandler(mHttpClient);
        // LocationManager and LocationListener
        mLocationManager = locationManager;
        mLocationListener = locationListener;
    }

    /**
     * Constructor
     * 
     * @param Context context
     * @param boolean autoInitialize
     * @param LocationManager locationManager
     * @param LocationListener locationListener
     */
    public SyncAdapter(Context context, boolean autoInitialize, LocationManager locationManager, LocationListener locationListener) {
        super(context, autoInitialize);
        mContext = context;
        mAccountManager = AccountManager.get(context);

        // Get the HTTP client
        mHttpClient = HttpFactory.getInstance();
        // Get the HTTP request handler
        mHttpHandler = new RequestHandler(mHttpClient);
        // LocationManager and LocationListener
        mLocationManager = locationManager;
        mLocationListener = locationListener;
    }

    /**
     * Encapsulate a sync to the server.
     * 
     * @param Account account
     * @param Bundle extras
     * @param ContentProviderClient provider
     * @param SyncResult syncResult
     */
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
            ContentProviderClient provider, SyncResult syncResult) {
        Log.v(TAG, "onPerformSync()");

        try {
            final ContentResolver resolver = mContext.getContentResolver();

            // Determines whether the whole sync was successful
            boolean success = true;

            // Get the auth token
            String authToken = mAccountManager.blockingGetAuthToken(account, Constants.AUTH_TOKEN_TYPE, true);

            // Sync Ownership Capsules
            String serverOwnershipCtag = mHttpHandler.requestCtag(authToken, RequestContract.Uri.CTAG_OWNERSHIPS_URI);
    
            String clientOwnershipCtag = mAccountManager.getUserData(account, "ownership_ctag");
            if (serverOwnershipCtag.equals(clientOwnershipCtag)) {
                // Sync dirty
                List<Capsule> capsules = CapsuleOperations.getOwnerships(resolver, account.name, true /* onlyDirty */);
                success = syncDirtyOwnerships(resolver, account, authToken, capsules);
            } else {
                // Get the server's Ownership Capsules
                String response = mHttpHandler.requestOwnershipStatus(authToken);
                List<Capsule> serverOwnerships = JSONParser.parseOwnershipStatus(response);
                // Get the client's Ownership Capsules
                List<Capsule> clientOwnerships = CapsuleOperations.getOwnerships(resolver, account.name, false /* onlyDirty */);
                // Perform a two-way sync of the Ownerships
                success = syncOwnerships(resolver, account, authToken, serverOwnerships, clientOwnerships);
            }

            // Ownership sync was successful, so update the Ctag
            if (success) {
                serverOwnershipCtag = mHttpHandler.requestCtag(authToken, RequestContract.Uri.CTAG_OWNERSHIPS_URI);
                if (!serverOwnershipCtag.equals(clientOwnershipCtag)) {
                    mAccountManager.setUserData(account, "ownership_ctag", serverOwnershipCtag);
                }
            }
        } catch (OperationCanceledException | AuthenticatorException | IOException | JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Syncs dirty Ownership Capsules to the server for the specified Account
     * 
     * @param resolver
     * @param account
     * @param authToken
     * @param capsules
     * @return
     */
    private boolean syncDirtyOwnerships(ContentResolver resolver, Account account, String authToken, List<Capsule> capsules) {
        boolean success = true;

        for (Capsule capsule : capsules) {
            final boolean isDeleted = ((CapsuleOwnershipPojo) capsule).getDeleted() >= 1;

            if (isDeleted) {
                if (capsule.getSyncId() > 0) {
                    try {
                        int deleteStatusCode = mHttpHandler.requestOwnershipDelete(authToken, capsule.getSyncId());
                        
                        if (deleteStatusCode == HttpStatus.SC_NO_CONTENT || deleteStatusCode == HttpStatus.SC_NOT_FOUND) {
                            if (!CapsuleOperations.deleteCapsule(resolver, capsule.getId())) {
                                success = false;
                            }
                        }
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        // Flag a failure
                        success = false;
                    }
                } else {
                    if (!CapsuleOperations.deleteCapsule(resolver, capsule.getId())) {
                        success = false;
                    }
                }
            } else {
                try {
                    // Push the Ownership Capsule to the server
                    success = success && pushOwnership(resolver, account, authToken, capsule);
                } catch (ParseException | IOException | JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    // Flag a failure
                    success = false;
                }
            }
        }

        return success;
    }

    /**
     * Performs a two-way sync for Ownership Capsules
     * 
     * @param resolver
     * @param account
     * @param authToken
     * @param serverOwnerships
     * @param clientOwnerships
     * @return
     */
    private boolean syncOwnerships(ContentResolver resolver, Account account, String authToken,
            List<Capsule> serverOwnerships, List<Capsule> clientOwnerships) {
        // The success flag
        boolean success = true;
        try {
//            System.out.println("=========SERVER OWNERSHIPS============");
//            for (int i = 0; i < serverOwnerships.size(); i++) {
//                System.out.println("=====================");
//                System.out.println(((CapsuleOwnershipPojo) serverOwnerships.get(i)).getSyncId());
//                if (((CapsuleOwnershipPojo) serverOwnerships.get(i)).getEtag() != null) {
//                    System.out.println(((CapsuleOwnershipPojo) serverOwnerships.get(i)).getEtag());
//                }
//            }
//            System.out.println("=========CLIENT OWNERSHIPS============");
//            for (int i = 0; i < clientOwnerships.size(); i++) {
//                System.out.println("=====================");
//                System.out.println(((CapsuleOwnershipPojo) clientOwnerships.get(i)).getSyncId());
//                if (((CapsuleOwnershipPojo) clientOwnerships.get(i)).getEtag() != null) {
//                    System.out.println(((CapsuleOwnershipPojo) clientOwnerships.get(i)).getEtag());
//                }
//            }
            // Determine those only on the client side
            List<Capsule> onLeft = new ArrayList<Capsule>();
            onLeft.addAll(clientOwnerships);
            onLeft.removeAll(serverOwnerships);
//            System.out.println("=========ONLY CLIENT SIDE============");
//            for (int i = 0; i < onLeft.size(); i++) {
//                System.out.println("=====================");
//                System.out.println(((CapsuleOwnershipPojo) onLeft.get(i)).getSyncId());
//                if (((CapsuleOwnershipPojo) onLeft.get(i)).getEtag() != null) {
//                    System.out.println(((CapsuleOwnershipPojo) onLeft.get(i)).getEtag());
//                }
//            }
            // Determine those only on the server side
            List<Capsule> onRight = new ArrayList<Capsule>();
            onRight.addAll(serverOwnerships);
            onRight.removeAll(clientOwnerships);
//            System.out.println("=========ONLY SERVER SIDE============");
//            for (int i = 0; i < onRight.size(); i++) {
//                System.out.println("=====================");
//                System.out.println(((CapsuleOwnershipPojo) onRight.get(i)).getSyncId());
//                if (((CapsuleOwnershipPojo) onRight.get(i)).getEtag() != null) {
//                    System.out.println(((CapsuleOwnershipPojo) onRight.get(i)).getEtag());
//                }
//            }
            // Determine those that are on both sides
            List<Capsule> onBoth = new ArrayList<Capsule>(clientOwnerships);
            onBoth.retainAll(serverOwnerships);
            // Will hold those that are on both sides and are up to date
            List<Capsule> onBothSame = new ArrayList<Capsule>();
            // Will hold those that are on both sides and are the out of date
            List<Capsule> onBothDifferent = new ArrayList<Capsule>();
//            System.out.println("=========ON BOTH SIDES============");
            for (int i = 0; i < onBoth.size(); i++) {
//                System.out.println("=====================");
//                System.out.println(((CapsuleOwnershipPojo) onBoth.get(i)).getSyncId());
//                if (((CapsuleOwnershipPojo) onBoth.get(i)).getEtag() != null) {
//                    System.out.println(((CapsuleOwnershipPojo) onBoth.get(i)).getEtag());
//                }
                // Get the client version
                Capsule clientCapsule = clientOwnerships.get(clientOwnerships.indexOf(onBoth.get(i)));
                // Get the server version
                Capsule serverCapsule = serverOwnerships.get(serverOwnerships.indexOf(onBoth.get(i)));
                // Determine if the ETag differs
                if (((CapsuleOwnershipPojo) clientCapsule).getEtag().equals(((CapsuleOwnershipPojo) serverCapsule).getEtag())) {
                    onBothSame.add(clientCapsule);
                } else {
                    onBothDifferent.add(clientCapsule);
                }
            }
//            System.out.println("=========ON BOTH SIDES -- SAME ============");
//            for (int i = 0; i < onBothSame.size(); i++) {
//                System.out.println("=====================");
//                System.out.println(((CapsuleOwnershipPojo) onBothSame.get(i)).getSyncId());
//                if (((CapsuleOwnershipPojo) onBothSame.get(i)).getEtag() != null) {
//                    System.out.println(((CapsuleOwnershipPojo) onBothSame.get(i)).getEtag());
//                }
//            }
//            System.out.println("=========ON BOTH SIDES -- DIFFERENT============");
//            for (int i = 0; i < onBothDifferent.size(); i++) {
//                System.out.println("=====================");
//                System.out.println(((CapsuleOwnershipPojo) onBothDifferent.get(i)).getSyncId());
//                if (((CapsuleOwnershipPojo) onBothDifferent.get(i)).getEtag() != null) {
//                    System.out.println(((CapsuleOwnershipPojo) onBothDifferent.get(i)).getEtag());
//                }
//            }

            // Will hold Capsules to be REPORTed on
            List<Capsule> reportCapsules = new ArrayList<Capsule>();

            // Capsules only on the client-side either need to be pushed to the server or deleted from the client
            for (Capsule capsule : onLeft) {
                final boolean isDirty = ((CapsuleOwnershipPojo) capsule).getDirty() > 0;
                final boolean isDeleted = ((CapsuleOwnershipPojo) capsule).getDeleted() > 0;
                if (isDirty && !isDeleted) {
                    // Push to the server
                    success = success && pushOwnership(resolver, account, authToken, capsule);
                } else {
                    // Delete from client
                    if (!CapsuleOperations.deleteCapsule(resolver, capsule.getId())) {
                        success = false;
                    }
                }
            }

            // Capsules only on the server-side need to be pulled to the client
            for (Capsule capsule : onRight) {
                // Pull it to the client
                reportCapsules.add(capsule);
            }

            // Capsules that are on both the client and server with the same ETag means the server-side hasn't changed, but the client-side may have
            for (Capsule capsule : onBothSame) {
                final boolean isDirty = ((CapsuleOwnershipPojo) capsule).getDirty() > 0;
                final boolean isDeleted = ((CapsuleOwnershipPojo) capsule).getDeleted() > 0;
                // Dirty Capsules should be pushed to the server
                if (isDirty) {
                    if (isDeleted) {
                        // Send a DELETE request
                        int statusCode = mHttpHandler.requestOwnershipDelete(authToken, capsule.getSyncId());
                        if (statusCode == HttpStatus.SC_NO_CONTENT || statusCode == HttpStatus.SC_NOT_FOUND) {
                            // On success, delete the client-side version
                            if (!CapsuleOperations.deleteCapsule(resolver, capsule.getId())) {
                                success = false;
                            }
                        } else {
                            success = false;
                        }
                    } else {
                        // Push the dirty Capsule to the server
                        success = success && pushOwnership(resolver, account, authToken, capsule);
                    }
                }
            }

            // Capsules that are on both the client and server with differing ETags means the server-side has changed, and the client-side may have
            for (Capsule capsule : onBothDifferent) {
                final boolean isDirty = ((CapsuleOwnershipPojo) capsule).getDirty() > 0;
                final boolean isDeleted = ((CapsuleOwnershipPojo) capsule).getDeleted() > 0;
                // Check if the Capsule has been changed on the client-side as well
                if (isDirty) {
                    // TODO Add the option for users to specify server or client taking priority
                    // For now, pull changes to client
                    reportCapsules.add(capsule);
                } else {
                    // Pull the changes to the client
                    reportCapsules.add(capsule);
                }
            }

            // REPORT on the Capsules
            // TODO Split up the request
            if (reportCapsules.size() > 0) {
                String response = mHttpHandler.requestOwnershipReport(authToken, reportCapsules);
                List<Capsule> reportedCapsules = JSONParser.parseOwnershipReport(response);
                for (Capsule capsule : reportedCapsules) {
                    success = success && CapsuleOperations.insertUpdateOwnership(resolver, account.name, capsule, false /* setDirty */);
                }
            }
        } catch (IOException | ParseException | JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return success;
    }

    /**
     * Pushes a Capsule Ownership to the server and updates the client-side
     * 
     * @param resolver
     * @param account
     * @param authToken
     * @param capsule
     * @return
     * @throws ParseException
     * @throws IOException
     * @throws JSONException
     */
    private boolean pushOwnership(ContentResolver resolver, Account account, String authToken, Capsule capsule)
            throws ParseException, IOException, JSONException {
        boolean success = true;
        // Request an update to the server
        String response = mHttpHandler.requestOwnershipUpdate(authToken, capsule);

        // Parse the response
        Capsule serverCapsule = JSONParser.parseOwnershipCapsule(response);

        // Update the client Capsule
        ContentValues values = new ContentValues();
        values.put(CapsuleContract.Capsules.SYNC_ID, serverCapsule.getSyncId());
        int count = resolver.update(
                CapsuleContract.Capsules.CONTENT_URI,
                values,
                CapsuleContract.Capsules._ID + " = ?",
                new String[]{String.valueOf(capsule.getId())}
        );
        if (count < 1) {
            success = false;
        }
        // Update the client Ownership
        values = new ContentValues();
        values.put(CapsuleContract.Capsules.DIRTY, 0);
        values.put(CapsuleContract.Capsules.ETAG, ((CapsuleOwnershipPojo) serverCapsule).getEtag());
        count = resolver.update(
                CapsuleContract.Ownerships.CONTENT_URI,
                values,
                CapsuleContract.Ownerships.ACCOUNT_NAME + " = ? AND " + CapsuleContract.Ownerships.CAPSULE_ID + " = ?",
                new String[]{account.name, String.valueOf(capsule.getId())}
        );
        if (count < 1) {
            success = false;
        }

        return success;
    }
}
