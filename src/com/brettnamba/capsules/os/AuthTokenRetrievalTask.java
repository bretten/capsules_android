package com.brettnamba.capsules.os;

import android.accounts.Account;
import android.util.Log;

/**
 * AsyncTask to handle retrieving the authentication token from the AccountManager
 */
public class AuthTokenRetrievalTask extends AsyncListenerTask<Account, Void, String> {

    /**
     * The listener that handles the callbacks
     */
    private AuthTokenRetrievalTaskListener mListener;

    /**
     * Logging tag
     */
    private final String TAG = "AuthTokenTask";

    /**
     * Constructor to be passed an Activity which implements the listener
     *
     * @param listener The listener that handles the callbacks
     */
    public AuthTokenRetrievalTask(TaskListener listener) {
        this.setListener(listener);
    }

    /**
     * Sets the listener for the AsyncTask
     *
     * @param listener The listener that handles the callbacks
     */
    @Override
    public void setListener(TaskListener listener) {
        try {
            this.mListener = (AuthTokenRetrievalTaskListener) listener;
        } catch (ClassCastException e) {
            throw new ClassCastException(listener.toString() + " does not implement AuthTokenRetrievalTaskListener");
        }
    }

    /**
     * Removes the listener that handles the callbacks
     */
    @Override
    public void removeListener() {
        this.mListener = null;
    }

    /**
     * Delegates the process to the listener's callback
     *
     * @param params The Account to retrieve the authentication token for
     * @return String The authentication token or null if it could not be found
     */
    @Override
    protected String doInBackground(Account... params) {
        Log.i(TAG, "doInBackground: " + this.hashCode());
        if (this.mListener != null) {
            return this.mListener.duringAuthTokenRetrieval(params);
        }
        return null;
    }

    /**
     * Delegates any pre-execution to the listener's callback
     */
    @Override
    protected void onPreExecute() {
        Log.i(TAG, "onPreExecute: " + this.hashCode());
        if (this.mListener != null) {
            this.mListener.onPreAuthTokenRetrieval();
        }
    }

    /**
     * Delegates any post-execution to the listener's callback
     *
     * @param authToken The authentication token
     */
    @Override
    protected void onPostExecute(String authToken) {
        Log.i(TAG, "onPostExecute: " + this.hashCode());
        if (this.mListener != null) {
            this.mListener.onPostAuthTokenRetrieval(authToken);
        }
    }

    /**
     * Delegates the cancellation process to the listener
     */
    @Override
    protected void onCancelled() {
        Log.i(TAG, "onCancelled: " + this.hashCode());
        if (this.mListener != null) {
            this.mListener.onAuthTokenRetrievalCancelled();
        }
    }

}
