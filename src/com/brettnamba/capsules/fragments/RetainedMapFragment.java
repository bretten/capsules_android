package com.brettnamba.capsules.fragments;

import android.accounts.Account;
import android.app.Activity;
import android.app.ProgressDialog;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.brettnamba.capsules.R;
import com.brettnamba.capsules.os.AsyncListenerTask;
import com.brettnamba.capsules.os.AuthTokenRetrievalTask;
import com.brettnamba.capsules.os.CapsulePingTask;

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
     * AsyncTask that retrieves undiscovered Capsules on the background thread
     */
    private CapsulePingTask mCapsulePingTask;

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
        if (this.mCapsulePingTask != null) {
            // Set the newly attached Activity as the listener for the Capsule ping task
            this.mCapsulePingTask.setListener((AsyncListenerTask.TaskListener) activity);
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
        if (this.mCapsulePingTask != null) {
            this.mCapsulePingTask.removeListener();
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
        this.cancelAuthTokenRetrieval();
        this.cancelCapsulePing();
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
     * Flags the authentication token task to be cancelled if it is running
     */
    public void cancelAuthTokenRetrieval() {
        Log.i(TAG, "cancelAuthTokenRetrieval()");
        if (this.mAuthTokenTask != null && this.mAuthTokenTask.getStatus() == AsyncTask.Status.RUNNING) {
            this.mAuthTokenTask.cancel(true);
            this.mAuthTokenTask = null;
        }
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
     * Starts a Capsule ping background task
     *
     * @param activity  The Activity to use as the listener
     * @param authToken The authentication token
     * @param location  Location object containing the user's location
     */
    public void startCapsulePing(Activity activity, String authToken, Location location) {
        Log.i(TAG, "startCapsulePing()");
        // Send a request for the undiscovered Capsules on the background thread
        if (this.mCapsulePingTask == null || this.mCapsulePingTask.getStatus() != AsyncTask.Status.RUNNING) {
            Log.i(TAG, "Looking for new capsules...");
            this.mCapsulePingTask = new CapsulePingTask((AsyncListenerTask.TaskListener) activity);
            String lat = Double.toString(location.getLatitude());
            String lng = Double.toString(location.getLongitude());
            this.mCapsulePingTask.execute(authToken, lat, lng);
        }
    }

    /**
     * Flags the Capsule ping background task to be cancelled
     */
    public void cancelCapsulePing() {
        Log.i(TAG, "cancelCapsulePing()");
        if (this.mCapsulePingTask != null && this.mCapsulePingTask.getStatus() == AsyncTask.Status.RUNNING) {
            this.mCapsulePingTask.cancel(true);
            this.mCapsulePingTask = null;
        }
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
