package com.brettnamba.capsules.os;

import com.brettnamba.capsules.dataaccess.CapsuleOwnership;
import com.brettnamba.tomoeame.os.AsyncListenerTask;

/**
 * AsyncTask that handles saving a owned Capsule
 */
public class OwnershipSaveTask extends AsyncListenerTask<CapsuleOwnership, Void, Boolean> {

    /**
     * The listener that handles the callbacks
     */
    private AsyncTaskListeners.OwnershipSaveTaskListener mListener;

    /**
     * Constructor that sets the listener
     *
     * @param listener The listener that handles the callbacks
     */
    public OwnershipSaveTask(TaskListener listener) {
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
            this.mListener = (AsyncTaskListeners.OwnershipSaveTaskListener) listener;
        } catch (ClassCastException e) {
            throw new ClassCastException(
                    listener.toString() + " does not implement OwnershipSaveTaskListener");
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
     * Delegates work to the listener
     *
     * @param params The Capsule being saved
     * @return The result of the save
     */
    @Override
    protected Boolean doInBackground(CapsuleOwnership... params) {
        if (this.mListener != null) {
            return this.mListener.duringOwnershipSave(params);
        }
        return null;
    }

    /**
     * Delegates work to the listener
     */
    @Override
    protected void onPreExecute() {
        if (this.mListener != null) {
            this.mListener.onPreOwnershipSave();
        }
    }

    /**
     * Delegates work to the listener
     *
     * @param success The result of the save
     */
    @Override
    protected void onPostExecute(Boolean success) {
        if (this.mListener != null) {
            this.mListener.onPostOwnershipSave(success);
        }
    }

    /**
     * Delegates work to the listener
     */
    @Override
    protected void onCancelled() {
        if (this.mListener != null) {
            this.mListener.onOwnershipSaveCancelled();
        }
    }

}
