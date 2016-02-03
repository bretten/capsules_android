package com.brettnamba.capsules.os;

import com.brettnamba.capsules.dataaccess.Capsule;
import com.brettnamba.tomoeame.os.AsyncListenerTask;

import java.util.List;

/**
 * AsyncTask that discovers all Capsules near the device's location
 */
public class DiscoverCapsulesTask extends AsyncListenerTask<Double, Void, List<Capsule>> {

    /**
     * Listener that will handle the callbacks
     */
    private AsyncTaskListeners.DiscoverCapsulesTaskListener mListener;

    /**
     * Constructor that sets the listener
     *
     * @param listener Listener that will handle the callbacks
     */
    public DiscoverCapsulesTask(AsyncTaskListeners.DiscoverCapsulesTaskListener listener) {
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
            this.mListener = (AsyncTaskListeners.DiscoverCapsulesTaskListener) listener;
        } catch (ClassCastException e) {
            throw new ClassCastException(
                    listener.toString() + " does not implement DiscoverCapsulesTaskListener");
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
     * Delegates the background work to the listener
     *
     * @param params The latitude and longitude of the device's location
     * @return The Capsules that were discovered
     */
    @Override
    protected List<Capsule> doInBackground(Double... params) {
        if (this.mListener != null) {
            return this.mListener.duringDiscoverCapsules(params);
        }
        return null;
    }

    /**
     * Delegates the work to the listener
     */
    @Override
    protected void onPreExecute() {
        if (this.mListener != null) {
            this.mListener.onPreDiscoverCapsules();
        }
    }

    /**
     * Delegates the process to the listener
     *
     * @param capsules The Capsules that were discovered
     */
    @Override
    protected void onPostExecute(List<Capsule> capsules) {
        if (this.mListener != null) {
            this.mListener.onPostDiscoverCapsules(capsules);
        }
    }

    /**
     * Delegates the work to the listener
     */
    @Override
    protected void onCancelled() {
        if (this.mListener != null) {
            this.mListener.onDiscoverCapsulesCancelled();
        }
    }

}
