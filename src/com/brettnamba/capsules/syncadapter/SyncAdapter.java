package com.brettnamba.capsules.syncadapter;

import java.io.IOException;
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

        final ContentResolver resolver = mContext.getContentResolver();

        // Get the auth token
        String authToken = "";
        try {
            authToken = mAccountManager.blockingGetAuthToken(account, Constants.AUTH_TOKEN_TYPE, true);
        } catch (OperationCanceledException | AuthenticatorException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Determines whether the whole sync was successful
        boolean success = true;

        // Sync Ownership Capsules
        String serverOwnershipCtag = null;
        try {
            serverOwnershipCtag = mHttpHandler.requestCtag(authToken, RequestContract.Uri.CTAG_OWNERSHIPS_URI);
        } catch (ParseException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String clientOwnershipCtag = mAccountManager.getUserData(account, "ownership_ctag");
        if (serverOwnershipCtag.equals(clientOwnershipCtag)) {
            // Sync dirty
            List<Capsule> capsules = CapsuleOperations.getOwnerships(resolver, account.name, true /* onlyDirty */);
            success = syncDirtyOwnerships(resolver, account, authToken, capsules);
        } else {
            // TODO Two-way sync
        }

        // Ownership sync was successful, so update the Ctag
        if (success) {
            try {
                serverOwnershipCtag = mHttpHandler.requestCtag(authToken, RequestContract.Uri.CTAG_OWNERSHIPS_URI);
            } catch (ParseException | IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (!serverOwnershipCtag.equals(clientOwnershipCtag)) {
                mAccountManager.setUserData(account, "ownership_ctag", serverOwnershipCtag);
            }
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
                    int deleteStatusCode = 0;
                    try {
                        deleteStatusCode = mHttpHandler.requestOwnershipDelete(authToken, capsule.getSyncId());
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    if (deleteStatusCode == HttpStatus.SC_NO_CONTENT || deleteStatusCode == HttpStatus.SC_NOT_FOUND) {
                        int count = resolver.delete(
                                CapsuleContract.Ownerships.CONTENT_URI,
                                CapsuleContract.Ownerships.CAPSULE_ID + " = ?",
                                new String[]{String.valueOf(capsule.getId())}
                        );
                        if (count > 0) {
                            count = resolver.delete(
                                    CapsuleContract.Capsules.CONTENT_URI,
                                    CapsuleContract.Capsules._ID + " = ?",
                                    new String[]{String.valueOf(capsule.getId())}
                            );
                            if (count < 1) {
                                success = false;
                            }
                        }
                    }
                } else {
                    int count = resolver.delete(
                            CapsuleContract.Ownerships.CONTENT_URI,
                            CapsuleContract.Ownerships.CAPSULE_ID + " = ?",
                            new String[]{String.valueOf(capsule.getId())}
                    );
                    if (count > 0) {
                        count = resolver.delete(
                                CapsuleContract.Capsules.CONTENT_URI,
                                CapsuleContract.Capsules._ID + " = ?",
                                new String[]{String.valueOf(capsule.getId())}
                        );
                        if (count < 1) {
                            success = false;
                        }
                    }
                }
            } else {
                // Request an update
                String response = null;
                try {
                    response = mHttpHandler.requestOwnershipUpdate(authToken, capsule);
                } catch (ParseException | IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                // Parse the response
                Capsule serverCapsule = null;
                try {
                    serverCapsule = JSONParser.parseOwnershipCapsule(response);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
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
            }
        }

        return success;
    }

}
