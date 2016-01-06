package com.brettnamba.capsules.activities;

import android.accounts.Account;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.brettnamba.capsules.R;
import com.brettnamba.capsules.dataaccess.Capsule;
import com.brettnamba.capsules.fragments.SortDialogFragment;
import com.brettnamba.capsules.http.CapsuleRequestParameters;
import com.brettnamba.capsules.http.RequestHandler;
import com.brettnamba.capsules.http.response.JsonResponse;
import com.brettnamba.capsules.os.AsyncListenerTask;
import com.brettnamba.capsules.os.GetCapsulesTask;
import com.brettnamba.capsules.os.RetainedTaskFragment;
import com.brettnamba.capsules.util.Widgets;
import com.brettnamba.capsules.widget.CapsuleArrayAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity that displays a user's Capsules.
 *
 * @author Brett Namba
 */
public abstract class CapsuleListActivity extends FragmentActivity implements
        AsyncListenerTask.GetCapsulesTaskListener,
        SortDialogFragment.SortDialogListener {

    /**
     * The current Account
     */
    protected Account mAccount;

    /**
     * Fragment used to retain background thread tasks
     */
    private RetainedTaskFragment mRetainingFragment;

    /**
     * The request parameters for retrieving Capsules from the Web API
     */
    protected CapsuleRequestParameters mRequestParams;

    /**
     * Flag that determines if the ListView is loading more Capsule items due to being scrolled
     * to the bottom of the list
     */
    private boolean mIsScrollLoading;

    /**
     * Adapter that loads the Capsule data
     */
    protected CapsuleArrayAdapter mAdapter;

    /**
     * Collection of Capsules that are displayed in the ListView
     */
    private ArrayList<Capsule> mCapsules;

    /**
     * Tag for storing the collection of Capsules in state data
     */
    private static final String TAG_CAPSULES = "capsules";

    /**
     * Tag for storing the request parameters in state data
     */
    private static final String TAG_REQUEST_PARAMS = "request_params";

    /**
     * Tag for the Fragment that retains background thread tasks
     */
    private static final String TAG_RETAINING_FRAGMENT = "retaining_fragment";

    /**
     * Tag for the Fragment that displays the sort options
     */
    private static final String TAG_SORT_DIALOG = "sort_dialog";

    /**
     * onCreate
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_capsule_list);

        // Get the Intent extras
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            this.mAccount = extras.getParcelable("account");
        }

        // Close the Activity if the required data was not present in the extras
        if (this.mAccount == null) {
            this.finish();
        }

        // Set up the Fragment that retains background thread tasks
        this.setupRetainedFragment();

        // Check if the Activity has any retained state data
        if (savedInstanceState != null) {
            // Get the retained request parameters
            this.mRequestParams = savedInstanceState.getParcelable(TAG_REQUEST_PARAMS);
            // Get the retained Capsules
            this.mCapsules = savedInstanceState.getParcelableArrayList(TAG_CAPSULES);
        } else {
            // Instantiate the request parameters
            this.mRequestParams = new CapsuleRequestParameters().setPage(1);
            // Instantiate the Capsule collection
            this.mCapsules = new ArrayList<Capsule>();
            // Request the initial Capsules
            this.requestCapsules(this.mRequestParams);
        }

        // Setup the ListView
        this.setupListView();

        // Setup the Toolbar
        this.setupToolbar();
    }

    /**
     * onSaveInstanceState
     *
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the request parameters
        outState.putParcelable(TAG_REQUEST_PARAMS, this.mRequestParams);
        // Save the Capsule collection
        outState.putParcelableArrayList(TAG_CAPSULES, this.mCapsules);
    }

    /**
     * Requests Capsules from the server and parses the response
     *
     * @param params Parameters for the request
     * @return The parsed collection of Capsules
     */
    @Override
    public abstract List<Capsule> duringGetCapsules(CapsuleRequestParameters params);

    /**
     * Executes before the request for Capsules is made
     */
    @Override
    public void onPreGetCapsules() {
    }

    /**
     * Takes the parsed Capsules from the server and adds them to the list
     *
     * @param capsules The parsed collection of Capsules returned from the request
     */
    @Override
    public void onPostGetCapsules(List<Capsule> capsules) {
        // Indicate that the Capsules have stopped loading
        this.mIsScrollLoading = false;

        if (capsules != null && capsules.size() > 0) {
            // Add the Capsules to the Adapter and internal collection
            for (Capsule capsule : capsules) {
                this.mCapsules.add(capsule);
                this.mAdapter.add(capsule);
            }
            // Notify changes
            this.mAdapter.notifyDataSetChanged();
            // Increment the request parameters page count for the next request
            this.mRequestParams.incrementPage();
        }
    }

    /**
     * Executes if the request for Capsules is cancelled
     */
    @Override
    public void onGetCapsulesCancelled() {
    }

    /**
     * Method that executes after the SortDialogFragment's positive button is clicked
     *
     * @param fragment A reference to the SortDialogFragment
     */
    @Override
    public void onConfirm(SortDialogFragment fragment) {
        // Remove all previous items in the list
        this.removeItems();
        // Build the request parameters that were selected
        this.mRequestParams = fragment.buildSortParameters();
        // Request new Capsules based on the new search params
        this.requestCapsules(this.mRequestParams);
    }

    /**
     * Method that executes after the SortDialogFragment's negative button is clicked
     *
     * @param fragment A reference to the SortDialogFragment
     */
    @Override
    public void onCancel(SortDialogFragment fragment) {
    }

    /**
     * Requests Capsules based on the specified request parameters
     *
     * @param params The Capsule request parameters
     */
    public void requestCapsules(CapsuleRequestParameters params) {
        // Set the params
        this.mRequestParams = params;
        // Instantiate the background task for getting the Capsules
        GetCapsulesTask task = new GetCapsulesTask(this);
        // Retain the Task on the retaining Fragment
        this.mRetainingFragment.setTask(task);
        // Execute the task on the background thread
        task.execute(params);
    }

    /**
     * Removes all Capsules from the internal member variable and clears the Adapter items
     */
    public void removeItems() {
        // Empty the Capsule collection
        this.mCapsules = new ArrayList<Capsule>();
        // Check that the Adapter is set
        if (this.mAdapter == null) {
            return;
        }
        // Remove all items from the Adapter
        this.mAdapter.clear();
        this.mAdapter.notifyDataSetChanged();
    }

    /**
     * Sets up the ListView's Adapter
     */
    protected abstract void setupListViewAdapter();

    /**
     * Sets up the Toolbar
     */
    protected abstract void setupToolbar();

    /**
     * Attempts to get RetainedTaskFragment if its state has been retained.  Otherwise, will
     * instantiate a new RetainedTaskFragment
     */
    private void setupRetainedFragment() {
        // Attempt to get the retaining Fragment
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        this.mRetainingFragment =
                (RetainedTaskFragment) fragmentManager.findFragmentByTag(TAG_RETAINING_FRAGMENT);
        // If the retaining Fragment was not found, this Fragment is being created for the first time
        if (this.mRetainingFragment == null) {
            // Instantiate the retaining Fragment
            this.mRetainingFragment = new RetainedTaskFragment();
            fragmentManager.beginTransaction().add(this.mRetainingFragment, TAG_RETAINING_FRAGMENT)
                    .commit();
        }
    }

    /**
     * Sets up the ListView and populates the Adapter with the specified Capsules
     */
    private void setupListView() {
        // Get a reference to the ListView
        ListView listView = (ListView) this.findViewById(R.id.capsule_list);
        // Instantiate the Adapter
        this.setupListViewAdapter();
        // Set the Adapter on the ListView
        listView.setAdapter(this.mAdapter);

        // Set the scroll listener
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
                // Determine if the ListView is being scrolled past the bottom item
                if (totalItemCount > 0 && firstVisibleItem + visibleItemCount >= totalItemCount) {
                    // Check if any Capsules are being loaded from a previous scroll
                    if (!CapsuleListActivity.this.mIsScrollLoading) {
                        // Set the scroll loading flag to active
                        CapsuleListActivity.this.mIsScrollLoading = true;
                        // Load more Capsules
                        CapsuleListActivity.this.requestCapsules(
                                CapsuleListActivity.this.mRequestParams);
                    }
                }
            }
        });
    }

    /**
     * Extension of CapsuleListActivity for Capsules
     */
    public static class CapsulesListActivity extends CapsuleListActivity {

        /**
         * Requests Capsules from the server and parses the response
         *
         * @param params Parameters for the request
         * @return The parsed collection of Capsules
         */
        @Override
        public List<Capsule> duringGetCapsules(CapsuleRequestParameters params) {
            JsonResponse response = RequestHandler.requestCapsules(this.getApplicationContext(),
                    this.mAccount, params);
            return response.getCapsules();
        }

        /**
         * Sets up the ListView's Adapter
         */
        @Override
        protected void setupListViewAdapter() {
            // Instantiate the Adapter
            this.mAdapter = new CapsuleArrayAdapter(this.getApplicationContext(),
                    R.layout.capsule_list_item);
        }

        /**
         * Sets up the Toolbar
         */
        @Override
        protected void setupToolbar() {
            // Setup the Toolbar
            Toolbar toolbar =
                    Widgets.createToolbar(this, this.getString(R.string.title_my_collection));
            toolbar.inflateMenu(R.menu.capsule_list_activity);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Close this Activity
                    CapsulesListActivity.this.finish();
                }
            });
            toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.action_sort:
                            // Launch the SortDialogFragment
                            SortDialogFragment fragment = SortDialogFragment.createInstance(
                                    CapsulesListActivity.this.mRequestParams);
                            fragment.show(CapsulesListActivity.this.getSupportFragmentManager(),
                                    CapsuleListActivity.TAG_SORT_DIALOG);
                            return true;
                        default:
                            return false;
                    }
                }
            });
        }
    }

    /**
     * Extension of CapsuleListActivity for Discovery Capsules
     */
    public static class DiscoveriesListActivity extends CapsuleListActivity {

        /**
         * Requests Discovery Capsules from the server and parses the response
         *
         * @param params Parameters for the request
         * @return The parsed collection of Capsules
         */
        @Override
        public List<Capsule> duringGetCapsules(CapsuleRequestParameters params) {
            JsonResponse response = RequestHandler.requestDiscoveries(this.getApplicationContext(),
                    this.mAccount, params);
            return response.getCapsules();
        }

        /**
         * Sets up the ListView's Adapter
         */
        @Override
        protected void setupListViewAdapter() {
            // Instantiate the Adapter
            this.mAdapter = new CapsuleArrayAdapter(this.getApplicationContext(),
                    R.layout.capsule_list_item);
        }

        /**
         * Sets up the Toolbar
         */
        @Override
        protected void setupToolbar() {
            // Setup the Toolbar
            Toolbar toolbar =
                    Widgets.createToolbar(this, this.getString(R.string.title_my_discoveries));
            toolbar.inflateMenu(R.menu.capsule_list_activity);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Close this Activity
                    DiscoveriesListActivity.this.finish();
                }
            });
            toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.action_sort:
                            // Launch the SortDialogFragment
                            SortDialogFragment fragment = SortDialogFragment.createInstance(
                                    DiscoveriesListActivity.this.mRequestParams);
                            fragment.show(DiscoveriesListActivity.this.getSupportFragmentManager(),
                                    CapsuleListActivity.TAG_SORT_DIALOG);
                            return true;
                        default:
                            return false;
                    }
                }
            });
        }
    }

}
