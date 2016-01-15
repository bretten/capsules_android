package com.brettnamba.capsules.fragments;

import android.accounts.Account;
import android.app.Activity;
import android.app.ProgressDialog;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.brettnamba.capsules.R;
import com.brettnamba.capsules.dataaccess.Capsule;
import com.brettnamba.capsules.os.AsyncListenerTask;
import com.brettnamba.capsules.os.AuthTokenRetrievalTask;
import com.brettnamba.capsules.os.CapsuleOpenTask;
import com.brettnamba.capsules.os.CapsulePingTask;
import com.brettnamba.capsules.os.DiscoverCapsulesTask;

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
     * AsyncTask that opens an undiscovered Capsule on the background thread
     */
    private CapsuleOpenTask mCapsuleOpenTask;

    /**
     * AsyncTask that discovers Capsules on the background thread
     */
    private DiscoverCapsulesTask mDiscoverCapsulesTask;

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
        if (this.mCapsuleOpenTask != null) {
            // Set the newly attached Activity as the listener
            this.mCapsuleOpenTask.setListener((AsyncListenerTask.TaskListener) activity);
        }
        if (this.mDiscoverCapsulesTask != null) {
            this.mDiscoverCapsulesTask.setListener((AsyncListenerTask.TaskListener) activity);
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
        if (this.mCapsuleOpenTask != null) {
            this.mCapsuleOpenTask.removeListener();
        }
        if (this.mDiscoverCapsulesTask != null) {
            this.mDiscoverCapsulesTask.removeListener();
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
        this.cancelCapsuleOpen();
        this.cancelDiscoverCapsules();
    }

    /**
     * Starts the task to retrieve the authentication token
     *
     * @param activity The Activity to use as the listener
     * @param account  The Account to get the authentication token for
     */
    public void startAuthTokenRetrieval(Activity activity, Account account) {
        Log.i(TAG, "startAuthTokenRetrieval()");
        // If the retained Account is different than the last used one, reset any Account-dependent members
        if (this.isAccountDifferent(account)) {
            // Cancel any running tasks
            this.cancelTasks();
            // Retain the new Account
            this.setAccount(account);
        }
        // If authentication is not already happening, do it on the background thread
        if (!this.isRetrievingAuthToken()) {
            // Get the auth token on the background thread
            this.mAuthTokenTask =
                    new AuthTokenRetrievalTask((AsyncListenerTask.TaskListener) activity);
            this.mAuthTokenTask.execute(account);
        }
    }

    /**
     * Flags the authentication token task to be cancelled if it is running
     */
    public void cancelAuthTokenRetrieval() {
        Log.i(TAG, "cancelAuthTokenRetrieval()");
        if (this.mAuthTokenTask != null &&
                this.mAuthTokenTask.getStatus() == AsyncTask.Status.RUNNING) {
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
        return this.mAuthTokenTask != null &&
                this.mAuthTokenTask.getStatus() == AsyncTask.Status.RUNNING;
    }

    /**
     * Starts a Capsule ping background task
     *
     * @param activity  The Activity to use as the listener
     * @param account   The Account to make the request for
     * @param authToken The authentication token
     * @param location  Location object containing the user's location
     */
    public void startCapsulePing(Activity activity, Account account, String authToken,
                                 Location location) {
        Log.i(TAG, "startCapsulePing()");
        if (this.isAccountDifferent(account)) {
            // Cancel any running tasks
            this.cancelTasks();
            // Retain the new Account
            this.setAccount(account);
        } else {
            // Send a request for the undiscovered Capsules on the background thread
            if (!this.isPingingCapsules()) {
                Log.i(TAG, "Looking for new capsules...");
                this.mCapsulePingTask =
                        new CapsulePingTask((AsyncListenerTask.TaskListener) activity);
                String lat = Double.toString(location.getLatitude());
                String lng = Double.toString(location.getLongitude());
                this.mCapsulePingTask.execute(authToken, lat, lng);
            }
        }
    }

    /**
     * Flags the Capsule ping background task to be cancelled
     */
    public void cancelCapsulePing() {
        Log.i(TAG, "cancelCapsulePing()");
        if (this.isPingingCapsules()) {
            this.mCapsulePingTask.cancel(true);
            this.mCapsulePingTask = null;
        }
    }

    /**
     * Checks if the background task for pinging Capsules is running
     *
     * @return True if it is running, false if it is not
     */
    public boolean isPingingCapsules() {
        return this.mCapsulePingTask != null &&
                this.mCapsulePingTask.getStatus() == AsyncTask.Status.RUNNING;
    }

    /**
     * Starts a Capsule open background task
     *
     * @param activity  The Activity to use as the listener
     * @param account   The Account to make the request for
     * @param authToken The authentication token
     * @param location  Location object containing the user's location
     * @param capsule   The Capsule to open
     */
    public void startCapsuleOpen(Activity activity, Account account, String authToken,
                                 Location location, Capsule capsule) {
        Log.i(TAG, "startCapsuleOpen()");
        if (this.isAccountDifferent(account)) {
            // Cancel any running tasks
            this.cancelTasks();
            // Retain the new Account
            this.setAccount(account);
        } else {
            // Send a request on the background thread to open the Capsule
            if (!this.isOpeningCapsule()) {
                Log.i(TAG, "Opening capsule...");
                this.mCapsuleOpenTask =
                        new CapsuleOpenTask((AsyncListenerTask.TaskListener) activity);
                String lat = Double.toString(location.getLatitude());
                String lng = Double.toString(location.getLongitude());
                String syncId = Long.toString(capsule.getSyncId());
                this.mCapsuleOpenTask.execute(authToken, syncId, lat, lng);
            }
        }
    }

    /**
     * Cancels the Capsule open background task if it is running
     */
    public void cancelCapsuleOpen() {
        Log.i(TAG, "cancelCapsuleOpen()");
        if (this.isOpeningCapsule()) {
            this.mCapsuleOpenTask.cancel(true);
            this.mCapsuleOpenTask = null;
        }
    }

    /**
     * Checks if the Capsule open background task is running
     *
     * @return True if it is running, otherwise false
     */
    public boolean isOpeningCapsule() {
        return this.mCapsuleOpenTask != null &&
                this.mCapsuleOpenTask.getStatus() == AsyncTask.Status.RUNNING;
    }

    /**
     * Starts the background thread task to discover Capsules
     *
     * @param activity The current Activity
     * @param account  The current Account
     * @param location The location used in the discovery request to the server
     */
    public void startDiscoverCapsules(Activity activity, Account account, Location location) {
        if (this.isAccountDifferent(account)) {
            // Cancel any running tasks
            this.cancelTasks();
            // Retain the new Account
            this.setAccount(account);
        } else {
            if (!this.isDiscoveringCapsules()) {
                this.mDiscoverCapsulesTask = new DiscoverCapsulesTask(
                        (AsyncListenerTask.DiscoverCapsulesTaskListener) activity);
                this.mDiscoverCapsulesTask.execute(location.getLatitude(), location.getLongitude());
            }
        }
    }

    /**
     * Cancels the background thread task that discovers the Capsules
     */
    public void cancelDiscoverCapsules() {
        if (this.isDiscoveringCapsules()) {
            this.mDiscoverCapsulesTask.cancel(true);
            this.mDiscoverCapsulesTask = null;
        }
    }

    /**
     * Determines if the background thread task for discovering Capsules is running
     *
     * @return True if it is running
     */
    public boolean isDiscoveringCapsules() {
        return this.mDiscoverCapsulesTask != null &&
                this.mDiscoverCapsulesTask.getStatus() == AsyncTask.Status.RUNNING;
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

    /**
     * Checks if the specified Account is different than the current Account member
     *
     * @param account The account to check
     * @return True if they are different, false if they are not
     */
    private boolean isAccountDifferent(Account account) {
        return this.mAccount == null || !account.name.equals(this.mAccount.name);
    }

    /**
     * Using the specified FragmentManager, will find the already added instance of
     * RetainedMapFragment by its tag.  If it does not exist, will instantiate it and add it to
     * the FragmentManager.
     *
     * @param fm The FragmentManager
     * @return The RetainedMapFragment found in the FragmentManager or a new instance
     */
    public static RetainedMapFragment findOrCreate(FragmentManager fm) {
        RetainedMapFragment fragment = (RetainedMapFragment) fm.findFragmentByTag(TAG);
        if (fragment == null) {
            fragment = new RetainedMapFragment();
            fm.beginTransaction().add(fragment, TAG).commit();
        }
        return fragment;
    }

}
