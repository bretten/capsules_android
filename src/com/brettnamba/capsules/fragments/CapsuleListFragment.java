package com.brettnamba.capsules.fragments;

import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.ListView;

import com.brettnamba.capsules.activities.CapsuleActivity;
import com.brettnamba.capsules.dataaccess.Capsule;
import com.brettnamba.capsules.dataaccess.CapsuleDiscovery;
import com.brettnamba.capsules.dataaccess.CapsuleOwnership;
import com.brettnamba.capsules.provider.CapsuleContract;

import java.util.HashMap;
import java.util.Map;

/**
 * Fragment that displays a list of Capsules.
 *
 * @author Brett Namba
 */
public class CapsuleListFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Adapter that loads the Capsule data
     */
    private SimpleCursorAdapter mAdapter;

    /**
     * The current Account name
     */
    private Account mAccount;

    /**
     * Flag to determine if the Capsules belong to the Account
     */
    private boolean mOwned;

    /**
     * The content URI
     */
    private Uri mUri;

    /**
     * The list query projection
     */
    private String[] mProjection;

    /**
     * The query selection
     */
    private String mSelection;

    /**
     * The query selection arguments
     */
    private String[] mSelectionArgs;

    /**
     * Mapping of Capsule ID to a Capsule so that Capsules can be retained
     * after they are fully loaded from the Cursor
     */
    private Map<Long, Capsule> mIdCapsuleMap;

    /**
     * Listener that handles the callbacks
     */
    private CapsuleListFragmentListener mListener;

    /**
     * Request code for starting CapsuleActivity
     */
    private static final int REQUEST_CODE_CAPSULE = 1;

    /**
     * onAttach
     *
     * @param activity The Activity the Fragment is being attached to
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Make sure the Activity implements this Fragment's listener
        try {
            this.mListener = (CapsuleListFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " does not implement CapsuleListFragmentListener");
        }
    }

    /**
     * onCreate
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the arguments passed in from the Activity
        Bundle args = this.getArguments();
        if (args != null) {
            this.mOwned = args.getBoolean("owned");
            this.mAccount = args.getParcelable("account");
            this.mUri = args.getParcelable("uri");
            this.mProjection = args.getStringArray("projection");
            this.mSelection = args.getString("selection");
            this.mSelectionArgs = args.getStringArray("selection_args");
        }

        // Instantiate the collection that will keep track of Capsules
        this.mIdCapsuleMap = new HashMap<Long, Capsule>();

        // Instantiate the Adapter
        this.mAdapter = new SimpleCursorAdapter(
                this.getActivity(),
                android.R.layout.simple_list_item_2,
                /* Cursor */ null,
                new String[]{CapsuleContract.Capsules.NAME, CapsuleContract.Discoveries.ACCOUNT_NAME},
                new int[]{android.R.id.text1, android.R.id.text2},
                /* flags */ 0
        );
        this.setListAdapter(this.mAdapter);

        // Get the Loader
        this.getLoaderManager().initLoader(0, null, this);
    }

    /**
     * onActivityCreated
     *
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // If the Fragment is missing any required data, delegate handling to the listener
        if (this.mAccount == null || this.mUri == null || this.mProjection == null
                || this.mSelection == null || this.mSelectionArgs == null) {
            this.mListener.onMissingData(this);
        }
    }

    /**
     * onActivityResult
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case (CapsuleListFragment.REQUEST_CODE_CAPSULE):
                if (resultCode == Activity.RESULT_OK) {
                    // Check if a Capsule was sent in the Intent data
                    Capsule capsule = data.getParcelableExtra("capsule");
                    if (capsule != null) {
                        // Place the Capsule from the Intent data into the collection
                        this.mIdCapsuleMap.put(capsule.getId(), capsule);
                    }
                }
                break;

            default:
                break;
        }

    }

    /**
     * onListItemClick
     *
     * @param l
     * @param v
     * @param position
     * @param id
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        // Get the Cursor at the position that was clicked
        Cursor c = (Cursor) this.getListView().getItemAtPosition(position);
        // See if the Capsule has already been instantiated and retained in the collection
        Capsule capsule;
        if (this.mIdCapsuleMap.containsKey(id)) {
            // Get the Capsule
            capsule = this.mIdCapsuleMap.get(id);
        } else {
            // Capsule could not be found, so instantiate one from the Cursor
            if (this.mOwned) {
                capsule = new CapsuleOwnership(c);
            } else {
                capsule = new CapsuleDiscovery(c);
            }
            // Add it to the collection
            this.mIdCapsuleMap.put(id, capsule);
        }
        // Start the Activity to view the Capsule
        Intent intent = new Intent(this.getActivity(), CapsuleActivity.class);
        intent.putExtra("owned", this.mOwned);
        intent.putExtra("capsule", capsule);
        intent.putExtra("account", this.mAccount);
        this.startActivityForResult(intent, CapsuleListFragment.REQUEST_CODE_CAPSULE);
    }

    /**
     * onCreateLoader
     *
     * @param id
     * @param args
     * @return
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = null;
        if (this.mAccount != null) {
            loader = new CursorLoader(
                    this.getActivity(),
                    this.mUri,
                    this.mProjection,
                    this.mSelection,
                    this.mSelectionArgs,
                    /* sortOrder */ null
            );
        }
        return loader;
    }

    /**
     * onLoadFinished
     *
     * @param loader
     * @param c
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
        // Determine the notification URI
        Uri notificationUri;
        if (this.mOwned) {
            notificationUri = CapsuleContract.Ownerships.CONTENT_URI;
        } else {
            notificationUri = CapsuleContract.Discoveries.CONTENT_URI;
        }
        // Set the notification URI
        c.setNotificationUri(this.getActivity().getContentResolver(), notificationUri);
        // Swap the Cursor
        this.mAdapter.swapCursor(c);
    }

    /**
     * onLoaderReset
     *
     * @param loader
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        this.mAdapter.swapCursor(null);
    }

    /**
     * Listener that provides event callbacks for this Fragment
     */
    public interface CapsuleListFragmentListener {

        /**
         * Should handle the case where the Fragment is not passed the required data
         *
         * @param capsuleListFragment The Fragment that is missing the data
         */
        void onMissingData(CapsuleListFragment capsuleListFragment);

    }

}
