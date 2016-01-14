package com.brettnamba.capsules.activities;

import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.LruCache;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.brettnamba.capsules.R;
import com.brettnamba.capsules.dataaccess.Capsule;
import com.brettnamba.capsules.dataaccess.Discovery;
import com.brettnamba.capsules.fragments.CapsuleFragment;
import com.brettnamba.capsules.fragments.DiscoveryFragment;
import com.brettnamba.capsules.http.HttpUrlGetRequest;
import com.brettnamba.capsules.http.RequestHandler;
import com.brettnamba.capsules.os.AsyncListenerTask;
import com.brettnamba.capsules.os.GetMemoirBitmapTask;
import com.brettnamba.capsules.os.RetainedBitmapCacheFragment;
import com.brettnamba.capsules.os.RetainedTaskFragment;
import com.brettnamba.capsules.os.UpdateDiscoveryTask;
import com.brettnamba.capsules.util.Images;
import com.brettnamba.capsules.util.Widgets;

/**
 * Activity that displays a Capsule and other Fragments depending on the type of the Capsule
 *
 * @author Brett Namba
 */
public class CapsuleActivity extends FragmentActivity implements
        CapsuleFragment.CapsuleFragmentListener,
        DiscoveryFragment.DiscoveryFragmentListener,
        AsyncListenerTask.GetMemoirBitmapTaskListener,
        AsyncListenerTask.UpdateDiscoveryTaskListener {

    /**
     * The Capsule for this Activity
     */
    private Capsule mCapsule;

    /**
     * Discovery copy that reflects the server-side state
     */
    private Discovery mServerDiscovery;

    /**
     * The Account that is opening this Activity
     */
    private Account mAccount;

    /**
     * Whether or not the Capsule was modified
     */
    private boolean mModified = false;

    /**
     * The Fragment for displaying a Capsule
     */
    private CapsuleFragment mCapsuleFragment;

    /**
     * The Fragment for displaying Discovery UI elements
     */
    private DiscoveryFragment mDiscoveryFragment;

    /**
     * Fragment to retain the background thread task for retrieving the Capsule's Memoir image
     */
    private RetainedTaskFragment mRetainedMemoirTaskFragment;

    /**
     * Fragment to retain background thread task for updating a Discovery Capsule
     */
    private RetainedTaskFragment mRetainedDiscoveryTaskFragment;

    /**
     * Bitmap cache for storing the Memoir image
     */
    private LruCache<String, Bitmap> mBitmapCache;

    /**
     * The FragmentManager tag for the Fragment that retains the Memoir image background thread
     * task
     */
    private static final String TAG_MEMOIR_FRAGMENT = "retained_memoir_fragment";

    /**
     * The FragmentManager tag for the Fragment that retains the Discovery update background
     * thread task
     */
    private static final String TAG_DISCOVERY_FRAGMENT = "retained_discovery_fragment";

    /**
     * onCreate
     *
     * @param savedInstanceState The previous state data or null if the Activity is new
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_capsule);

        // Get the Intent extras
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            this.mCapsule = extras.getParcelable("capsule");
            this.mAccount = extras.getParcelable("account");
        }
        // See if state data was found
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("capsule")) {
                this.mCapsule = savedInstanceState.getParcelable("capsule");
            }
            if (savedInstanceState.containsKey("modified")) {
                this.mModified = savedInstanceState.getBoolean("modified");
            }
        }

        // Close the Activity if required members are missing
        if (this.mCapsule == null || this.mAccount == null) {
            this.finish();
        }

        // Setup the Fragments
        this.setupFragments(savedInstanceState);

        // Setup the Toolbar
        this.setupToolbar();
    }

    /**
     * onResume
     */
    @Override
    protected void onResume() {
        super.onResume();

        // Request the Memoir image
        if (this.mCapsule.getMemoir() != null && this.mCapsule.getMemoir().getSyncId() > 0) {
            // Get the Memoir server side ID
            long memoirId = this.mCapsule.getMemoir().getSyncId();
            // Check to see if the Memoir image is cached
            Bitmap bitmap = this.mBitmapCache.get(String.valueOf(memoirId));
            if (bitmap == null) {
                // No Bitmap was cached, so request the image from the server
                this.requestMemoirImage(memoirId);
            } else {
                // The Bitmap was found in the cache, so place it in a View
                this.mCapsuleFragment.setMemoirImageView(bitmap);
            }
        }
    }

    /**
     * onSaveInstanceState
     *
     * @param outState State data to be saved
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the Capsule in the state data
        outState.putParcelable("capsule", this.mCapsule);
        outState.putBoolean("modified", this.mModified);
    }

    /**
     * onBackPressed
     */
    @Override
    public void onBackPressed() {
        this.setOkResult();
        super.onBackPressed();
    }

    /**
     * Callback for when the CapsuleFragment is not passed the required data
     *
     * Will close this Activity
     *
     * @param capsuleFragment The Fragment that is missing the data
     */
    @Override
    public void onMissingData(CapsuleFragment capsuleFragment) {
        this.getSupportFragmentManager().beginTransaction().remove(capsuleFragment).commit();
        this.setResult(Activity.RESULT_CANCELED);
        this.finish();
        Toast.makeText(this, this.getString(R.string.error_cannot_open_missing_data),
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Should handle the case where the Fragment is not passed the required data
     *
     * @param discoveryFragment The Fragment that is missing data
     */
    @Override
    public void onMissingData(DiscoveryFragment discoveryFragment) {
        this.getSupportFragmentManager().beginTransaction().remove(discoveryFragment).commit();
        this.setResult(Activity.RESULT_CANCELED);
        this.finish();
        Toast.makeText(this, this.getString(R.string.error_cannot_open_missing_data),
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Executed when the DiscoveryFragment's RatingControl is rated up
     */
    @Override
    public void onRateUp() {
        if (!this.mCapsule.isDiscovery()) {
            return;
        }
        Discovery discovery = this.mCapsule.getDiscovery();
        // Copy the original state of the Discovery
        this.copyServerDiscoveryState(discovery);
        // Update the Discovery
        discovery.setRating(1);
        // Request the update to the server
        this.requestDiscoveryUpdate();
    }

    /**
     * Executed when the DiscoveryFragment's RatingControl is rated down
     */
    @Override
    public void onRateDown() {
        if (!this.mCapsule.isDiscovery()) {
            return;
        }
        Discovery discovery = this.mCapsule.getDiscovery();
        // Copy the original state of the Discovery
        this.copyServerDiscoveryState(discovery);
        // Update the Discovery
        discovery.setRating(-1);
        // Request the update to the server
        this.requestDiscoveryUpdate();
    }

    /**
     * Executed when the DiscoveryFragment's RatingControl is set to no rating
     */
    @Override
    public void onRemoveRating() {
        if (!this.mCapsule.isDiscovery()) {
            return;
        }
        Discovery discovery = this.mCapsule.getDiscovery();
        // Copy the original state of the Discovery
        this.copyServerDiscoveryState(discovery);
        // Update the Discovery
        discovery.setRating(0);
        // Request the update to the server
        this.requestDiscoveryUpdate();
    }

    /**
     * Executed when the DiscoveryFragment's favorite input is changed
     *
     * @param isFavorite True if it was set as a favorite, false if it was unset as a favorite
     */
    @Override
    public void onFavorite(boolean isFavorite) {
        if (!this.mCapsule.isDiscovery()) {
            return;
        }
        Discovery discovery = this.mCapsule.getDiscovery();
        // Copy the original state of the Discovery
        this.copyServerDiscoveryState(discovery);
        // Update the Discovery
        discovery.setIsFavorite(isFavorite);
        // Request the update to the server
        this.requestDiscoveryUpdate();
    }

    /**
     * Called on the background thread when the the Memoir image is being retrieved from the server
     *
     * @param memoirId The server-side ID of the Memoir
     * @return The Memoir's Bitmap
     */
    @Override
    public Bitmap duringGetMemoirBitmap(long memoirId) {
        try {
            // Open a connection to the Memoir image
            HttpUrlGetRequest request = RequestHandler
                    .requestMemoirImage(this.getApplicationContext(), this.mAccount, memoirId);
            // Convert the image to a Bitmap
            Bitmap bitmap = BitmapFactory.decodeStream(request.getResponseStream());
            // Scale the Bitmap
            bitmap = Images.scaleBitmap(this, bitmap, /* widthScaleFactor */ 1);
            // Close the connection
            request.close();

            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Called on the main thread before the Memoir image is retrieved on the background thread
     */
    @Override
    public void onPreGetMemoirBitmap() {
    }

    /**
     * Called on the main thread after the Memoir image is retrieved on the background thread
     *
     * @param bitmap The constructed Bitmap from the server's image data
     */
    @Override
    public void onPostGetMemoirBitmap(Bitmap bitmap) {
        // Save the Bitmap to the cache
        this.mBitmapCache.put(String.valueOf(this.mCapsule.getMemoir().getSyncId()), bitmap);
        // Display the Bitmap
        this.mCapsuleFragment.setMemoirImageView(bitmap);
    }

    /**
     * Called on the main thread if the background thread task for retrieving the Memoir image is
     * cancelled
     */
    @Override
    public void onGetMemoirBitmapCancelled() {
    }

    /**
     * Called on the background thread while the background thread task for updating a Discovery
     * is executing
     *
     * @param params The update data for the Discovery
     * @return The result of the update
     */
    @Override
    public boolean duringUpdateDiscovery(Discovery... params) {
        return RequestHandler.updateDiscovery(this, this.mAccount, params[0]);
    }

    /**
     * Called on the main thread before the background thread task for updating a Discovery is
     * executed
     */
    @Override
    public void onPreUpdateDiscovery() {
        // Disable the rating buttons in the DiscoveryFragment
        if (this.mDiscoveryFragment != null) {
            this.mDiscoveryFragment.disableButtons();
        }
    }

    /**
     * Called on the main thread after the background thread task for updating a Discovery has
     * executed
     *
     * @param result The result of the update
     */
    @Override
    public void onPostUpdateDiscovery(Boolean result) {
        // Check the result
        if (result != null && result && this.mCapsule.isDiscovery()) {
            // The result was a success, so the updated Discovery now reflects the server-side state
            this.mServerDiscovery = this.mCapsule.getDiscovery();
            // Flag that the Capsule was updated
            this.mModified = true;
        } else {
            // The result was not a success, so revert to the server-side state
            this.mCapsule.setDiscovery(this.mServerDiscovery);
            // Reset the view to match the Discovery
            if (this.mDiscoveryFragment != null) {
                this.mDiscoveryFragment.matchStateToDiscovery(this.mServerDiscovery);
            }
        }
        // Enable the rating buttons in the Discovery Fragment
        if (this.mDiscoveryFragment != null) {
            this.mDiscoveryFragment.enableButtons();
        }
    }

    /**
     * Called on the main thread after the background thread task for updating a Discovery has
     * been cancelled
     */
    @Override
    public void onUpdateDiscoveryCancelled() {
        // Enable the rating buttons in the Discovery Fragment
        if (this.mDiscoveryFragment != null) {
            this.mDiscoveryFragment.enableButtons();
        }
    }

    /**
     * Executes a background thread task to update the Discovery
     */
    private void requestDiscoveryUpdate() {
        if (!this.mCapsule.isDiscovery()) {
            return;
        }
        // Instantiate the background thread task for updating a Discovery
        UpdateDiscoveryTask task = new UpdateDiscoveryTask(this);
        // Add the background thread task to a Fragment that retains its instance
        this.mRetainedDiscoveryTaskFragment.setTask(task);
        // Execute the task
        task.execute(this.mCapsule.getDiscovery());
    }

    /**
     * Executes a background thread task to request the Memoir image from the server
     *
     * @param memoirId The server-side ID of the Memoir
     */
    private void requestMemoirImage(long memoirId) {
        if (this.mRetainedMemoirTaskFragment == null) {
            return;
        }
        // Instantiate the background thread task for getting the Memoir image
        GetMemoirBitmapTask task = new GetMemoirBitmapTask(this);
        // Retain the task on a Fragment
        this.mRetainedMemoirTaskFragment.setTask(task);
        // Execute the background thread process
        task.execute(memoirId);
    }

    /**
     * Sets up the Toolbar
     */
    private void setupToolbar() {
        // Setup the Toolbar
        Toolbar toolbar = Widgets.createToolbar(this, this.mCapsule.getName(), false);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close this Activity
                CapsuleActivity.this.setOkResult();
                CapsuleActivity.this.finish();
            }
        });
    }

    /**
     * Sets up the Fragments for this Activity
     *
     * @param savedInstanceState The previous state data or null if the Activity is new
     */
    private void setupFragments(Bundle savedInstanceState) {
        // FragmentManager
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        // Get the Fragment for retaining the background thread task for getting a Memoir image
        this.mRetainedMemoirTaskFragment =
                RetainedTaskFragment.findOrCreate(fragmentManager, TAG_MEMOIR_FRAGMENT);
        // Get the Fragment for retaining the Bitmap cache
        RetainedBitmapCacheFragment bitmapCacheFragment =
                RetainedBitmapCacheFragment.findOrCreate(fragmentManager);
        // Get the Bitmap cache
        this.mBitmapCache = bitmapCacheFragment.getCache();

        // Add the CapsuleFragment
        this.mCapsuleFragment = CapsuleFragment.createInstance(this.mCapsule, this.mAccount);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (savedInstanceState == null) {
            fragmentTransaction.add(R.id.fragment_capsule, this.mCapsuleFragment);
        } else {
            fragmentTransaction.replace(R.id.fragment_capsule, this.mCapsuleFragment);

        }
        fragmentTransaction.commit();

        // Setup any Discovery-related Fragments
        this.setupDiscoveryFragments(savedInstanceState, fragmentManager);
    }

    /**
     * Sets up Discovery-related Fragments
     *
     * @param savedInstanceState The previous state data or null if the Activity is new
     * @param fragmentManager    The FragmentManager
     */
    private void setupDiscoveryFragments(Bundle savedInstanceState,
                                         FragmentManager fragmentManager) {
        if (!this.mCapsule.isDiscovery()) {
            return;
        }

        // Get the Fragment for retaining the background thread task for updating a Discovery
        this.mRetainedDiscoveryTaskFragment =
                RetainedTaskFragment.findOrCreate(fragmentManager, TAG_DISCOVERY_FRAGMENT);

        // Instantiate the Fragment
        this.mDiscoveryFragment = DiscoveryFragment.createInstance(this.mCapsule, this.mAccount);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (savedInstanceState == null) {
            fragmentTransaction.add(R.id.fragment_discovery, this.mDiscoveryFragment);
        } else {
            fragmentTransaction.replace(R.id.fragment_discovery, this.mDiscoveryFragment);

        }
        fragmentTransaction.commit();
    }

    /**
     * Copies the server-side state of the Discovery
     *
     * @param discovery The Discovery to copy
     */
    private void copyServerDiscoveryState(Discovery discovery) {
        this.mServerDiscovery = new Discovery(discovery);
    }

    /**
     * Adds data to pass back when the Activity is closed under correct circumstances and
     * sets the result code as OK
     */
    private void setOkResult() {
        Intent intent = new Intent();
        intent.putExtra("capsule", this.mCapsule);
        intent.putExtra("modified", this.mModified);
        this.setResult(Activity.RESULT_OK, intent);
    }

}
