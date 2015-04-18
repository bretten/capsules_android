package com.brettnamba.capsules.os;

import android.content.Context;

import com.brettnamba.capsules.http.response.AuthenticationResponse;

/**
 * AsyncTask to handle registering a new user via the API
 */
public class AuthenticationTask extends AsyncListenerTask<String, Void, AuthenticationResponse> {

    /**
     * The listener that handles the callbacks
     */
    private AuthenticationTaskListener mListener;

    /**
     * Constructor to be passed an Activity which implements the listener
     *
     * @param context Activity that implements the listener
     */
    public AuthenticationTask(Context context) {
        this.mListener = (AuthenticationTaskListener) context;
    }

    /**
     * Sets the listener
     *
     * @param listener Listener that handles the callbacks
     */
    @Override
    public void setListener(TaskListener listener) {
        this.mListener = (AuthenticationTaskListener) listener;
    }

    /**
     * Delegates the process to the listener's callback
     *
     * @param params Credentials for authentication
     * @return The result of the authentication request
     */
    @Override
    protected AuthenticationResponse doInBackground(String... params) {
        if (this.mListener != null) {
            return this.mListener.duringAuthentication(params);
        }
        return null;
    }

    /**
     * Delegates any pre-execution to the listener's callback
     */
    @Override
    protected void onPreExecute() {
        if (this.mListener != null) {
            this.mListener.onPreAuthentication();
        }
    }

    /**
     * Delegates handling the authentication response to the listener
     *
     * @param response The HTTP response of the authentication
     */
    @Override
    protected void onPostExecute(AuthenticationResponse response) {
        if (this.mListener != null) {
            this.mListener.onPostAuthentication(response);
        }
    }

    /**
     * Delegates the cancellation process to the listener
     */
    @Override
    protected void onCancelled() {
        if (this.mListener != null) {
            this.mListener.onAuthenticationCancelled();
        }
    }

}
