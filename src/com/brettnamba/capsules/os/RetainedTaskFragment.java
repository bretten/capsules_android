package com.brettnamba.capsules.os;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.brettnamba.capsules.R;

/**
 * Fragment that is used to retain a reference to an AsyncTask.  The state is retained
 * by setting setRetainInstance(true).  When this Fragment is attached to an Activity it sets
 * the references to the newly attached Activity.  When the Fragment is detached from an Activity
 * it removes all references to the detached Activity.  As a result the Fragment will never
 * reference an Activity that may be destroyed.
 */
public class RetainedTaskFragment extends Fragment {

    /**
     * The AsyncTask that is retained
     */
    private AsyncListenerTask mTask;

    /**
     * The progress indicator to be displayed
     */
    private ProgressDialog mProgressDialog;

    /**
     * onCreate
     *
     * Sets the instance of to be retained throughout the parent Activity's lifecycle
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Flag the Fragment to be retained
        this.setRetainInstance(true);
    }

    /**
     * onAttach
     *
     * Associates any references to the new Activity
     *
     * @param activity The Activity that the Fragment is being attached to
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Instantiate a new ProgressDialog
        this.mProgressDialog = new ProgressDialog(activity);
        this.mProgressDialog.setMessage(activity.getString(R.string.progress_please_wait));
        if (this.mTask != null) {
            // Set the newly attached Activity as the listener for the AsyncTask
            this.mTask.setListener((AsyncListenerTask.TaskListener) activity);
            // If the AsyncTask is running, show the progress indicator
            if (this.mTask.getStatus() == AsyncTask.Status.RUNNING) {
                this.mProgressDialog.show();
            }
        }
    }

    /**
     * onDetach
     *
     * Removes all references to the Activity being detached
     */
    @Override
    public void onDetach() {
        super.onDetach();
        // Cancel the ProgressDialog and clear it out
        if (this.mProgressDialog != null) {
            this.mProgressDialog.cancel();
            this.mProgressDialog = null;
        }
        // Remove the listener from the AsyncTask
        this.mTask.removeListener();
    }

    /**
     * Sets the AsyncTask
     *
     * @param task The AsyncTask to be retained
     */
    public void setTask(AsyncListenerTask task) {
        this.mTask = task;
    }

    /**
     * Shows the progress indicator if it exists
     */
    public void showProgress() {
        if (this.mProgressDialog != null) {
            this.mProgressDialog.show();
        }
    }

    /**
     * Stops the progress indicator if it exists
     */
    public void hideProgress() {
        if (this.mProgressDialog != null) {
            this.mProgressDialog.cancel();
        }
    }

}
