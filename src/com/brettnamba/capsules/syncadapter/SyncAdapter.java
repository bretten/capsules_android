package com.brettnamba.capsules.syncadapter;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

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
     * The tag used for logging.
     */
    private static final String TAG = "SyncAdapter";

    /**
     * Constructor for Android 3.0 and later platforms
     * 
     * @param Context context
     * @param boolean autoInitialize
     * @param boolean allowParallelSyncs
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContext = context;
        mAccountManager = AccountManager.get(context);
    }

    /**
     * Constructor
     * 
     * @param Context context
     * @param boolean autoInitialize
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
        mAccountManager = AccountManager.get(context);
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
    }

}
