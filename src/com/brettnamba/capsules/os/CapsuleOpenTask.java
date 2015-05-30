package com.brettnamba.capsules.os;

import com.brettnamba.capsules.http.response.CapsuleOpenResponse;

/**
 * AsyncTask that opens an undiscovered Capsule
 */
public class CapsuleOpenTask extends AsyncListenerTask<String, Void, CapsuleOpenResponse> {

    /**
     * Listener that will handle the callbacks
     */
    private CapsuleOpenTaskListener mListener;

    /**
     * Constructor that sets the listener
     *
     * @param listener
     */
    public CapsuleOpenTask(TaskListener listener) {
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
            this.mListener = (CapsuleOpenTaskListener) listener;
        } catch (ClassCastException e) {
            throw new ClassCastException(listener.toString() + " does not implement CapsuleOpenTaskListener");
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
     * Delegates the background thread work to the listener
     *
     * @param params The user's authentication token, location, and the Capsule sync ID
     * @return HTTP response object representing opening a Capsule
     */
    @Override
    protected CapsuleOpenResponse doInBackground(String... params) {
        if (this.mListener != null) {
            return this.mListener.duringCapsuleOpen(params);
        }
        return null;
    }

    /**
     * Delegates work to the listener
     */
    @Override
    protected void onPreExecute() {
        if (this.mListener != null) {
            this.mListener.onPreCapsuleOpen();
        }
    }

    /**
     * Delegates work to the listener
     *
     * @param response HTTP response object representing an opened Capsule
     */
    @Override
    protected void onPostExecute(CapsuleOpenResponse response) {
        if (this.mListener != null) {
            this.mListener.onPostCapsuleOpen(response);
        }
    }

    /**
     * Delegates work to the listener
     */
    @Override
    protected void onCancelled() {
        if (this.mListener != null) {
            this.mListener.onCapsuleOpenCancelled();
        }
    }

}
