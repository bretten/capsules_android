package com.brettnamba.capsules.syncadapter;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.annotation.TargetApi;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.brettnamba.capsules.Constants;
import com.brettnamba.capsules.http.HttpFactory;
import com.brettnamba.capsules.http.RequestHandler;

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

        // Get the HTTP client
        mHttpClient = HttpFactory.getInstance();
        // Get the HTTP request handler
        mHttpHandler = new RequestHandler(mHttpClient);
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

        // Get the HTTP client
        mHttpClient = HttpFactory.getInstance();
        // Get the HTTP request handler
        mHttpHandler = new RequestHandler(mHttpClient);
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

        // Get the auth token
        String authToken = "";
        try {
            authToken = mAccountManager.blockingGetAuthToken(account, Constants.AUTH_TOKEN_TYPE, true);
        } catch (OperationCanceledException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (AuthenticatorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        // Send a request to the server for the undiscovered capsules
        JSONArray capsules = null;
        try {
            capsules = mHttpHandler.getUndiscoveredCapsules(authToken, 47.533119, -122.162415);
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        for (int i = 0; i < capsules.length(); i++) {
            JSONObject item = null;
            try {
                item = capsules.getJSONObject(i);
            } catch (JSONException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            try {
                JSONObject capsule = item.getJSONObject("Capsule");
                Log.v(TAG, capsule.getString("id"));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
