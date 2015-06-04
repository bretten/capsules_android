package com.brettnamba.capsules.syncadapter;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.annotation.TargetApi;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.brettnamba.capsules.Constants;
import com.brettnamba.capsules.dataaccess.CapsuleOwnership;
import com.brettnamba.capsules.http.HttpFactory;
import com.brettnamba.capsules.http.RequestContract;
import com.brettnamba.capsules.http.RequestHandler;
import com.brettnamba.capsules.http.response.CtagResponse;
import com.brettnamba.capsules.http.response.EntityDeleteResponse;
import com.brettnamba.capsules.http.response.OwnershipCollectionResponse;
import com.brettnamba.capsules.http.response.OwnershipResponse;
import com.brettnamba.capsules.provider.CapsuleContract;
import com.brettnamba.capsules.provider.CapsuleOperations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * SyncAdapter handles keeping Capsule data in sync with the web server
 *
 * @author Brett Namba
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    /**
     * AccountManager
     */
    private final AccountManager mAccountManager;

    /**
     * ContentResolver
     */
    private final ContentResolver mContentResolver;

    /**
     * ContentOperations builder
     */
    private final CapsuleOperations mCapsuleOperations;

    /**
     * Handles sending HTTP requests
     */
    private final RequestHandler mRequestHandler;

    /**
     * LocationManager for accessing system location
     */
    private final LocationManager mLocationManager;

    /**
     * Receives location updates
     */
    private final LocationListener mLocationListener;

    /**
     * Key for storing the Ownerships ctag in the AccountManager's user data
     */
    private static final String USER_DATA_KEY_OWNERSHIP_CTAG = "ctag_ownerships";

    /**
     * The limit of Capsules to have in a single HTTP request
     */
    private static final int CAPSULE_REQUEST_LIMIT = 50;

    /**
     * The tag used for logging.
     */
    private static final String TAG = "SyncAdapter";

    /**
     * Constructor for Android 3.0 and later platforms
     *
     * @param context
     * @param autoInitialize
     * @param allowParallelSyncs
     * @param locationManager
     * @param locationListener
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs,
                       LocationManager locationManager, LocationListener locationListener) {
        super(context, autoInitialize, allowParallelSyncs);
        // ContentResolver
        this.mContentResolver = context.getContentResolver();
        // CapsuleOperations
        this.mCapsuleOperations = new CapsuleOperations(this.mContentResolver);
        // AccountManager
        this.mAccountManager = AccountManager.get(context);
        // Get the HTTP request handler
        this.mRequestHandler = new RequestHandler(HttpFactory.getInstance());
        // LocationManager and LocationListener
        this.mLocationManager = locationManager;
        this.mLocationListener = locationListener;
    }

    /**
     * Constructor
     *
     * @param context
     * @param autoInitialize
     * @param locationManager
     * @param locationListener
     */
    public SyncAdapter(Context context, boolean autoInitialize, LocationManager locationManager, LocationListener locationListener) {
        super(context, autoInitialize);
        // ContentResolver
        this.mContentResolver = context.getContentResolver();
        // CapsuleOperations
        this.mCapsuleOperations = new CapsuleOperations(this.mContentResolver);
        // AccountManager
        this.mAccountManager = AccountManager.get(context);
        // Get the HTTP request handler
        this.mRequestHandler = new RequestHandler(HttpFactory.getInstance());
        // LocationManager and LocationListener
        this.mLocationManager = locationManager;
        this.mLocationListener = locationListener;
    }

    /**
     * Executes a sync to the server
     *
     * @param account
     * @param extras
     * @param authority
     * @param provider
     * @param syncResult
     */
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        Log.i(TAG, "onPerformSync()");
        // Get the auth token
        try {
            final String authToken = this.mAccountManager.blockingGetAuthToken(account,
                    Constants.AUTH_TOKEN_TYPE, true);

            // Sync the Account's Ownerships
            this.syncOwnerships(account, authToken);
        } catch (OperationCanceledException | AuthenticatorException | IOException e) {
            Log.e(TAG, "onPerformSync(): Auth token could not be retrieved");
        }
    }

    /**
     * Syncs the Account's Ownerships.  Determines if it needs to only push local changes to the
     * server (if the client and server ctag match) or if it needs to pull changes from the server
     * as well (if the client and server ctag differ).
     *
     * @param account   The Account to perform the sync for
     * @param authToken The Account's authentication token
     * @return True if the sync was successful, otherwise false
     */
    private boolean syncOwnerships(Account account, String authToken) {
        // Flag for keeping track of the success state
        boolean success;

        try {
            // Get the server ctag
            CtagResponse response = new CtagResponse(
                    this.mRequestHandler.requestCtag(authToken, RequestContract.Uri.CTAG_OWNERSHIPS_URI)
            );
            String serverCtag = null;
            if (response.isSuccess()) {
                serverCtag = response.getCtag();
            }
            // Get the client ctag
            String clientCtag = this.mAccountManager.getUserData(account, SyncAdapter.USER_DATA_KEY_OWNERSHIP_CTAG);
            // Determine which kind of sync
            if (serverCtag != null && clientCtag != null && serverCtag.equals(clientCtag)) {
                // The client is up-to-date with the server, so just push local changes to the server
                success = this.syncDirtyOwnerships(account, authToken);
            } else {
                // The client needs to get updates from the server, so perform a two-way sync
                success = this.syncOwnershipsTwoWay(account, authToken);
            }

            // Apply the remaining ContentProviderOperations as a batch
            this.mCapsuleOperations.applyBatch();

            // If the sync was successful, get the new Ownerships ctag and save it to the client
            if (success) {
                // Get the server ctag
                response = new CtagResponse(
                        this.mRequestHandler.requestCtag(authToken, RequestContract.Uri.CTAG_OWNERSHIPS_URI)
                );
                if (response.isSuccess()) {
                    serverCtag = response.getCtag();
                    // Save the ctag
                    this.mAccountManager.setUserData(account,
                            SyncAdapter.USER_DATA_KEY_OWNERSHIP_CTAG, serverCtag);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "syncOwnerships(): " + e.getMessage());
            success = false;
        }

        return success;
    }

    /**
     * Pushes any locally modified Capsule Ownerships to the server
     *
     * @param account   The Account to perform the sync for
     * @param authToken The Account's authentication token
     * @return True if the whole sync was successful, otherwise false
     */
    private boolean syncDirtyOwnerships(Account account, String authToken) {
        // Flag for keeping track of the success state
        boolean success = true;
        // Get the locally modified Capsules
        List<CapsuleOwnership> capsules = CapsuleOperations.Ownerships.get(this.mContentResolver,
                account, CapsuleContract.SyncStateAction.DIRTY);
        // Sync
        if (capsules != null && capsules.size() > 0) {
            for (CapsuleOwnership capsule : capsules) {
                success = success && this.syncDirtyOwnership(capsule, authToken);
            }
        }
        return success;
    }

    /**
     * Performs a two-way sync by comparing the client's and server's Capsule ownerships and
     * determines what needs to be pushed to the server and what needs to be pulled from the server
     *
     * @param account   The Account to perform the sync for
     * @param authToken The Account's authentication token
     * @return True if the whole sync was a success, otherwise false
     */
    private boolean syncOwnershipsTwoWay(Account account, String authToken) {
        // Flag for keeping track of the success state
        boolean success;

        try {
            // Get the all of the user's Capsules
            List<CapsuleOwnership> clientCapsules = CapsuleOperations.Ownerships.get(this.mContentResolver,
                    account, CapsuleContract.SyncStateAction.NONE);
            // Get all of the user's Capsules on the server
            OwnershipCollectionResponse response = new OwnershipCollectionResponse(
                    this.mRequestHandler.requestOwnershipStatus(authToken)
            );
            List<CapsuleOwnership> serverCapsules;
            if (!response.isSuccess()) {
                // The server capsules could not be retrieved, so do nothing
                success = false;
            } else {
                // Get the parsed server Capsules
                serverCapsules = response.getCapsules();
                // Compare the two collections of Capsules and determine how to sync each one
                success = this.compareOwnerships(clientCapsules, serverCapsules, account, authToken);
            }
        } catch (IOException e) {
            Log.e(TAG, "syncOwnershipsTwoWay(): " + e.getMessage());
            success = false;
        }

        return success;
    }

    /**
     * Compares the Capsule Ownership collections from the client and the server and determines
     * how to sync each one individually
     *
     * @param clientCapsules The client-side Capsules
     * @param serverCapsules The server-side Capsules
     * @param account        The Account to perform the sync for
     * @param authToken      The Account's authentication token
     * @return
     */
    private boolean compareOwnerships(List<CapsuleOwnership> clientCapsules,
                                      List<CapsuleOwnership> serverCapsules, Account account,
                                      String authToken) {
        // Flag for keeping track of the success state
        boolean success = true;

        try {
            // Determine the Capsules only on the client-side
            List<CapsuleOwnership> onClient = new ArrayList<CapsuleOwnership>();
            onClient.addAll(clientCapsules);
            onClient.removeAll(serverCapsules);
            // Determine the Capsules only on the server-side
            List<CapsuleOwnership> onServer = new ArrayList<CapsuleOwnership>();
            onServer.addAll(serverCapsules);
            onServer.removeAll(clientCapsules);
            // Determine Capsules that are on both the client and server
            List<CapsuleOwnership> onBoth = new ArrayList<CapsuleOwnership>();
            onBoth.addAll(clientCapsules);
            onBoth.retainAll(serverCapsules);
            // Determine Capsules that are up-to-date with the server and those that are not
            List<CapsuleOwnership> onBothSameEtag = new ArrayList<CapsuleOwnership>();
            List<CapsuleOwnership> onBothOutOfSync = new ArrayList<CapsuleOwnership>();
            for (int i = 0; i < onBoth.size(); i++) {
                // Get the server and client versions
                CapsuleOwnership commonCapsule = onBoth.get(i);
                CapsuleOwnership client = clientCapsules.get(clientCapsules.indexOf(commonCapsule));
                CapsuleOwnership server = serverCapsules.get(serverCapsules.indexOf(commonCapsule));
                // Determine if the etags differ
                if (client.getEtag().equals(server.getEtag())) {
                    // Their etags match, so the client side has the most recent server data
                    onBothSameEtag.add(commonCapsule);
                } else {
                    // Their etags don't match, so the server has newer data
                    onBothOutOfSync.add(commonCapsule);
                }
            }

            // Will hold the Capsules to REPORT on
            List<CapsuleOwnership> reportCapsules = new ArrayList<CapsuleOwnership>();

            // Capsules only on the client-side need to be pushed to the server or deleted from the client
            for (CapsuleOwnership capsule : onClient) {
                success = success && this.syncDirtyOwnership(capsule, authToken);
            }

            // Capsules only on the server-side need to be pulled from the server to the client
            for (CapsuleOwnership capsule : onServer) {
                // Add the Capsule to the collection of Capsules to REPORT on
                reportCapsules.add(capsule);
            }

            // Capsules that are on both the client and server with the same etag means
            // the server-side hasn't changed, but the client-side may have
            for (CapsuleOwnership capsule : onBothSameEtag) {
                // Check to see if it is dirty
                if (capsule.getDirty() > 0) {
                    // It is dirty, so there are local changes that need to be pushed to the server
                    success = success && this.syncDirtyOwnership(capsule, authToken);
                }
            }

            // Capsules that are on both the client and server with differing etags means
            // the server-side has changed and the client-side may have changed
            for (CapsuleOwnership capsule : onBothOutOfSync) {
                // Determine if the local Capsule has changed
                if (capsule.getDirty() > 0) {
                    // TODO Possibly allow the user to set a preference for the server or client taking priority
                    // Add the Capsule to the collection of Capsules to REPORT on
                    reportCapsules.add(capsule);
                } else {
                    // The client Capsule has not changed so, add the Capsule to the collection of Capsules to REPORT on
                    reportCapsules.add(capsule);
                }
            }

            // REPORT on the Capsules
            if (reportCapsules.size() > 0) {
                success = success && this.reportOwnerships(reportCapsules, account, authToken);
            }
        } catch (Exception e) {
            Log.e(TAG, "compareOwnerships() " + e.toString() + ": " + e.getMessage());
            success = false;
        }

        return success;
    }

    /**
     * Syncs a local CapsuleOwnership to the server
     *
     * @param capsule   The Capsule to sync
     * @param authToken The authentication token
     * @return True on a successful sync, otherwise false
     */
    private boolean syncDirtyOwnership(CapsuleOwnership capsule, String authToken) {
        // Flag for keeping track of the success state
        boolean success;

        // Check if the Capsule has been deleted
        final boolean isDeleted = capsule.getDeleted() >= 1;
        if (isDeleted) {
            // The Capsule has been flagged for deletion, so delete it
            success = this.deleteDirtyOwnership(capsule, authToken);
        } else {
            // The Capsule was not deleted locally, it was modified or created
            success = this.pushDirtyOwnership(capsule, authToken);
        }

        return success;
    }

    /**
     * Deletes a local CapsuleOwnership.  If it exists on the server, it will also send a request
     * to delete it on the server
     *
     * @param capsule   The Capsule to delete
     * @param authToken The authentication token
     * @return True on success, otherwise false
     */
    private boolean deleteDirtyOwnership(CapsuleOwnership capsule, String authToken) {
        // Flag for keeping track of the success state
        boolean success = true;

        try {
            // See if the Capsule has a server sync ID
            if (capsule.getSyncId() > 0) {
                // It has a sync ID, so attempt to delete it on the server first
                EntityDeleteResponse response = new EntityDeleteResponse(
                        this.mRequestHandler.requestOwnershipDelete(authToken, capsule.getSyncId())
                );
                // If the request was a success, build a Capsule DELETE operation
                if (response.isSuccess()) {
                    this.mCapsuleOperations.buildCapsuleDelete(capsule, /* withYield */ true);
                } else {
                    success = false;
                }
            } else {
                // It is a local Capsule, so only need to build a Capsule DELETE operation
                this.mCapsuleOperations.buildCapsuleDelete(capsule, /* withYield */ true);
            }
        } catch (IOException e) {
            Log.e(TAG, "deleteDirtyOwnership(): " + e.getMessage());
            success = false;
        }

        // Check and apply the ContentProviderOperations as a batch if necessary
        this.mCapsuleOperations.checkAndApply();

        return success;
    }

    /**
     * Pushes local modifications for a CapsuleOwnership to the server
     *
     * @param capsule   The Capsule to sync
     * @param authToken The authentication token
     * @return True on success, otherwise false
     */
    private boolean pushDirtyOwnership(CapsuleOwnership capsule, String authToken) {
        // Flag for keeping track of the success state
        boolean success = true;

        try {
            // Send the request to update the Capsule to the server and get the response
            OwnershipResponse response = new OwnershipResponse(
                    this.mRequestHandler.requestOwnershipUpdate(authToken, capsule)
            );
            // Check if the request was a success
            if (!response.isSuccess()) {
                success = false;
            } else {
                // Get the Capsule from the response
                CapsuleOwnership responseCapsule = response.getCapsule();
                // Add the new etag to the Capsule
                capsule.setEtag(responseCapsule.getEtag());
                // Add the new sync ID
                if (responseCapsule.getSyncId() > 0) {
                    capsule.setSyncId(responseCapsule.getSyncId());
                }
                // Build operations to save both the Capsule and Ownership
                this.mCapsuleOperations.buildOwnershipSave(capsule, CapsuleContract.SyncStateAction.CLEAN);
            }
        } catch (IOException e) {
            Log.e(TAG, "pushDirtyOwnership(): " + e.getMessage());
            success = false;
        }

        // Check and apply the ContentProviderOperations as a batch if necessary
        this.mCapsuleOperations.checkAndApply();

        return success;
    }

    /**
     * Given a collection of CapsuleOwnerships, will send their IDs to the server to get the most
     * up-to-date versions of them, parse them from the response, and then update the client-side
     * with the new server-side data
     *
     * @param capsules  The collection of Capsules to get updates for
     * @param account   The Account to assign any new Capsules to
     * @param authToken The authentication token
     * @return True if the whole operation was a success, false otherwise
     */
    private boolean reportOwnerships(List<CapsuleOwnership> capsules, Account account, String authToken) {
        // Flag for keeping track of the success state
        boolean success = true;

        try {
            // Split up the report Capsules into smaller collections for the HTTP request
            for (int i = 0; i < capsules.size(); i += SyncAdapter.CAPSULE_REQUEST_LIMIT) {
                // Get the chunk
                List<CapsuleOwnership> chunk = capsules.subList(i,
                        Math.min(i + SyncAdapter.CAPSULE_REQUEST_LIMIT, capsules.size()));
                // Send the request
                OwnershipCollectionResponse response = new OwnershipCollectionResponse(
                        this.mRequestHandler.requestOwnershipReport(authToken, chunk)
                );
                // Check if the response was a success
                if (!response.isSuccess()) {
                    success = false;
                } else {
                    // Get the Capsules from the response
                    List<CapsuleOwnership> responseCapsules = response.getCapsules();
                    // Save the Capsules
                    for (CapsuleOwnership capsule : responseCapsules) {
                        // Set the Account
                        capsule.setAccountName(account.name);
                        // Build operations to save both the Capsule and Ownership
                        this.mCapsuleOperations.buildOwnershipSave(capsule,
                                CapsuleContract.SyncStateAction.CLEAN);
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "reportOwnerships(): " + e.getMessage());
            success = false;
        }

        // Check and apply the ContentProviderOperations as a batch if necessary
        this.mCapsuleOperations.checkAndApply();

        return success;
    }

}
