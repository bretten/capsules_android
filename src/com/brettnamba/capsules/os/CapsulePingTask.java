package com.brettnamba.capsules.os;

import com.brettnamba.capsules.http.response.CapsulePingResponse;

/**
 * AsyncTask that gets undiscovered Capsules near the user's location
 */
public class CapsulePingTask extends AsyncListenerTask<String, Void, CapsulePingResponse> {

    /**
     * Listener that will handle the callbacks
     */
    private CapsulePingTaskListener mListener;

    /**
     * Constructor that sets the listener
     *
     * @param listener Listener that will handle the callbacks
     */
    public CapsulePingTask(TaskListener listener) {
        this.setListener(listener);
    }

    /**
     * Sets the listener for the AsyncTask
     *
     * @param listener The listener that handles the callbacks
     */
    @Override
    public void setListener(TaskListener listener) {
        this.mListener = (CapsulePingTaskListener) listener;
    }

    /**
     * Removes the listener that handles the callbacks
     */
    @Override
    public void removeListener() {
        this.mListener = null;
    }

    /**
     * Delegates the background work to the listener
     *
     * @param params The user's authentication token and location
     * @return Capsule ping HTTP response object
     */
    @Override
    protected CapsulePingResponse doInBackground(String... params) {
        if (this.mListener != null) {
            return this.mListener.duringCapsulePing(params);
        }
        return null;
    }

    /**
     * Delegates the work to the listener
     */
    @Override
    protected void onPreExecute() {
        if (this.mListener != null) {
            this.mListener.onPreCapsulePing();
        }
    }

    /**
     * Delegates the process to the listener
     *
     * @param response Capsule ping HTTP response object
     */
    @Override
    protected void onPostExecute(CapsulePingResponse response) {
        if (this.mListener != null) {
            this.mListener.onPostCapsulePing(response);
        }
    }

    /**
     * Delegates the work to the listener
     */
    @Override
    protected void onCancelled() {
        if (this.mListener != null) {
            this.mListener.onCapsulePingCancelled();
        }
    }

}
