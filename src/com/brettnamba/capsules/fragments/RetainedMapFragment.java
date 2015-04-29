package com.brettnamba.capsules.fragments;

import android.accounts.Account;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.brettnamba.capsules.R;
import com.brettnamba.capsules.os.AsyncListenerTask;
import com.brettnamba.capsules.os.AuthTokenRetrievalTask;

/**
 * Fragment to be retained along with the main Activity that displays the map
 */
public class RetainedMapFragment extends Fragment {

    /**
     * The retained Account
     */
    private Account mAccount;

    /**
     * AsyncTask that retrieves the authentication token on the background thread
     */
    private AuthTokenRetrievalTask mAuthTokenTask;

    /**
     * Progress indicator to show when a background task is running
     */
    private ProgressDialog mProgressDialog;

    /**
     * Log
     */
    private final static String TAG = "RetainedMapFragment";

    /**
     * onCreate
     *
     * Sets itself to be retained throughout the hosting Activity's lifecycle
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate()");
        super.onCreate(savedInstanceState);

        // Flag the Fragment to be retained
        this.setRetainInstance(true);
    }

    /**
     * onAttach
     *
     * @param activity The Activity the Fragment is being attached to
     */
    @Override
    public void onAttach(Activity activity) {
        Log.i(TAG, "onAttach()");
        super.onAttach(activity);
        // Instantiate a new ProgressDialog
        this.mProgressDialog = new ProgressDialog(activity);
        this.mProgressDialog.setMessage(activity.getString(R.string.progress_please_wait));
        // Associate the new Activity as the new listener for any AsyncTasks
        if (this.mAuthTokenTask != null) {
            // Set the newly attached Activity as the listener for the auth token task
            this.mAuthTokenTask.setListener((AsyncListenerTask.TaskListener) activity);
            // If the auth token task is running, show the progress indicator
            if (this.mAuthTokenTask.getStatus() == AsyncTask.Status.RUNNING) {
                this.mProgressDialog.show();
            }
        }
    }

    /**
     * onDetach
     */
    @Override
    public void onDetach() {
        Log.i(TAG, "onDetach()");
        super.onDetach();
        // Cancel the ProgressDialog and clear it out
        if (this.mProgressDialog != null) {
            this.mProgressDialog.cancel();
            this.mProgressDialog = null;
        }
        // Remove the listener from the AsyncTask
        if (this.mAuthTokenTask != null) {
            this.mAuthTokenTask.removeListener();
        }
    }

    /**
     * Gets the retained Account
     *
     * @return The retained Account
     */
    public Account getAccount() {
        Log.i(TAG, "getAccount()");
        return this.mAccount;
    }

    /**
     * Sets the Account to retain
     *
     * @param account The Account to retain
     */
    public void setAccount(Account account) {
        Log.i(TAG, "setAccount()");
        this.mAccount = account;
    }

    /**
     * Cancels any running AsyncTasks
     */
    public void cancelTasks() {
        Log.i(TAG, "cancelTasks()");
        if (this.mAuthTokenTask != null && this.mAuthTokenTask.getStatus() == AsyncTask.Status.RUNNING) {
            this.mAuthTokenTask.cancel(true);
            this.mAuthTokenTask = null;
        }
    }

    /**
     * Starts the task to retrieve the authentication token
     *
     * @param activity The Activity to use as the listener
     * @param account The Account to get the authentication token for
     */
    public void startAuthTokenRetrieval(Activity activity, Account account) {
        Log.i(TAG, "startAuthTokenRetrieval()");
        // Get the auth token on the background thread
        this.mAuthTokenTask = new AuthTokenRetrievalTask((AsyncListenerTask.TaskListener) activity);
        this.mAuthTokenTask.execute(account);
    }

    /**
     * Checks if the authentication token is being retrieved
     *
     * @return True if it is, false if it is not
     */
    public boolean isRetrievingAuthToken() {
        if (this.mAuthTokenTask != null) {
            return this.mAuthTokenTask.getStatus() == AsyncTask.Status.RUNNING;
        }
        return false;
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
