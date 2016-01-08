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
import com.brettnamba.capsules.fragments.CapsuleFragment;
import com.brettnamba.capsules.http.HttpUrlGetRequest;
import com.brettnamba.capsules.http.RequestHandler;
import com.brettnamba.capsules.os.AsyncListenerTask;
import com.brettnamba.capsules.os.GetMemoirBitmapTask;
import com.brettnamba.capsules.os.RetainedBitmapCacheFragment;
import com.brettnamba.capsules.os.RetainedTaskFragment;
import com.brettnamba.capsules.util.Images;
import com.brettnamba.capsules.util.Widgets;

/**
 * Activity that displays a Capsule and other Fragments depending on the type of the Capsule
 *
 * @author Brett Namba
 */
public class CapsuleActivity extends FragmentActivity implements
        CapsuleFragment.CapsuleFragmentListener,
        AsyncListenerTask.GetMemoirBitmapTaskListener {

    /**
     * The Capsule for this Activity
     */
    private Capsule mCapsule;

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
     * Fragment to retain the background thread task for retrieving the Capsule's Memoir image
     */
    private RetainedTaskFragment mRetainedTaskFragment;

    /**
     * Bitmap cache for storing the Memoir image
     */
    private LruCache<String, Bitmap> mBitmapCache;

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
     * Executes a background thread task to request the Memoir image from the server
     *
     * @param memoirId The server-side ID of the Memoir
     */
    private void requestMemoirImage(long memoirId) {
        if (this.mRetainedTaskFragment == null) {
            return;
        }
        // Instantiate the background thread task for getting the Memoir image
        GetMemoirBitmapTask task = new GetMemoirBitmapTask(this);
        // Retain the task on a Fragment
        this.mRetainedTaskFragment.setTask(task);
        // Execute the background thread process
        task.execute(memoirId);
    }

    /**
     * Sets up the Toolbar
     */
    private void setupToolbar() {
        // Setup the Toolbar
        Toolbar toolbar = Widgets.createToolbar(this, this.mCapsule.getName());
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
        this.mRetainedTaskFragment = RetainedTaskFragment.findOrCreate(fragmentManager);
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
