package com.brettnamba.capsules.os;

import com.brettnamba.capsules.dataaccess.Capsule;
import com.brettnamba.capsules.http.CapsuleRequestParameters;

import java.util.List;

/**
 * AsyncTask that requests a collection of Capsules from the web service and then parses the
 * results
 */
public class GetCapsulesTask
        extends AsyncListenerTask<CapsuleRequestParameters, Void, List<Capsule>> {

    /**
     * Listener that will handle the callbacks
     */
    private GetCapsulesTaskListener mListener;

    /**
     * Constructor that sets the listener
     *
     * @param listener Listener that will handle the callbacks
     */
    public GetCapsulesTask(GetCapsulesTaskListener listener) {
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
            this.mListener = (GetCapsulesTaskListener) listener;
        } catch (ClassCastException e) {
            throw new ClassCastException(
                    listener.toString() + " does not implement GetCapsulesTaskListener");
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
     * @param params The parameters to use in the request
     * @return The parsed collection of Capsules
     */
    @Override
    protected List<Capsule> doInBackground(CapsuleRequestParameters... params) {
        if (this.mListener != null) {
            return this.mListener.duringGetCapsules(params[0]);
        }
        return null;
    }

    /**
     * Delegates the work to the listener
     */
    @Override
    protected void onPreExecute() {
        if (this.mListener != null) {
            this.mListener.onPreGetCapsules();
        }
    }

    /**
     * Delegates the process to the listener
     *
     * @param capsules The parsed collection of Capsules returned from the request
     */
    @Override
    protected void onPostExecute(List<Capsule> capsules) {
        if (this.mListener != null) {
            this.mListener.onPostGetCapsules(capsules);
        }
    }

    /**
     * Delegates the work to the listener
     */
    @Override
    protected void onCancelled() {
        if (this.mListener != null) {
            this.mListener.onGetCapsulesCancelled();
        }
    }

}
