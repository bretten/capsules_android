package com.brettnamba.capsules.os;

import com.brettnamba.capsules.http.response.JsonResponse;
import com.brettnamba.tomoeame.os.AsyncListenerTask;

/**
 * AsyncTask to handle registering a new user via the API
 */
public class AuthenticationTask extends AsyncListenerTask<String, Void, JsonResponse> {

    /**
     * The listener that handles the callbacks
     */
    private AsyncTaskListeners.AuthenticationTaskListener mListener;

    /**
     * Constructor to be passed the listener
     *
     * @param listener Listener that handles the callbacks
     */
    public AuthenticationTask(TaskListener listener) {
        this.setListener(listener);
    }

    /**
     * Sets the listener
     *
     * @param listener Listener that handles the callbacks
     */
    @Override
    public void setListener(TaskListener listener) {
        try {
            this.mListener = (AsyncTaskListeners.AuthenticationTaskListener) listener;
        } catch (ClassCastException e) {
            throw new ClassCastException(
                    listener.toString() + " does not implement AuthenticationTaskListener");
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
     * @param params Credentials for authentication
     * @return The result of the authentication request
     */
    @Override
    protected JsonResponse doInBackground(String... params) {
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
    protected void onPostExecute(JsonResponse response) {
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
