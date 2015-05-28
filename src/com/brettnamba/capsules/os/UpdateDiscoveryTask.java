package com.brettnamba.capsules.os;

import com.brettnamba.capsules.dataaccess.CapsuleDiscoveryPojo;

/**
 * AsyncTask that handles updating a Discovery on the background thread
 */
public class UpdateDiscoveryTask extends AsyncListenerTask<CapsuleDiscoveryPojo, Void, Boolean> {

    /**
     * Listener that will handle callbacks
     */
    private UpdateDiscoveryTaskListener mListener;

    /**
     * Constructor that sets the listener
     *
     * @param listener Listener that handles the callbacks
     */
    public UpdateDiscoveryTask(TaskListener listener) {
        this.setListener(listener);
    }

    /**
     * Sets the listener for the AsyncTask
     *
     * @param listener The listener that handles the callbacks
     */
    @Override
    public void setListener(TaskListener listener) {
        this.mListener = (UpdateDiscoveryTaskListener) listener;
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
     * @param params The Discovery to update
     * @return The result of the update
     */
    @Override
    protected Boolean doInBackground(CapsuleDiscoveryPojo... params) {
        if (this.mListener != null) {
            return this.mListener.duringUpdateDiscovery(params);
        }
        return null;
    }

    /**
     * Delegates the work to the listener
     */
    @Override
    protected void onPreExecute() {
        if (this.mListener != null) {
            this.mListener.onPreUpdateDiscovery();
        }
    }

    /**
     * Delegates the process to the listener
     *
     * @param result Result of the update
     */
    @Override
    protected void onPostExecute(Boolean result) {
        if (this.mListener != null) {
            this.mListener.onPostUpdateDiscovery(result);
        }
    }

    /**
     * Delegates the work to the listener
     */
    @Override
    protected void onCancelled() {
        if (this.mListener != null) {
            this.mListener.onUpdateDiscoveryCancelled();
        }
    }

}
