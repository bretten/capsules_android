package com.brettnamba.capsules.fragments;

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
import com.brettnamba.capsules.dataaccess.CapsulePojo;
import com.brettnamba.capsules.provider.CapsuleContract;

/**
 * Fragment that displays a list of Capsules.
 * 
 * @author Brett Namba
 *
 */
public class CapsuleListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Reference to the Adapter for the List.
     */
    private SimpleCursorAdapter mAdapter;

    /**
     * The current Account name.
     */
    private String mAccountName;

    /**
     * Whether or not the list is of Owned Capsules
     */
    private boolean mOwned;

    /**
     * The content Uri
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
     * Whether or not a Capsule was modified
     */
    private boolean mModified = false;

    /**
     * Request code for CapsuleEditorActivity
     */
    private static final int REQUEST_CODE_CAPSULE_EDITOR = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get passed Bundle
        mOwned = getArguments().getBoolean("owned");
        mAccountName = getArguments().getString("account_name");
        mUri = (Uri) getArguments().getParcelable("uri");
        mProjection = getArguments().getStringArray("projection");
        mSelection = getArguments().getString("selection");
        mSelectionArgs = getArguments().getStringArray("selection_args");

        // Create the Adapter
        mAdapter = new SimpleCursorAdapter(
                getActivity(),
                android.R.layout.simple_list_item_2,
                null,
                mProjection,
                new int[]{android.R.id.text1, android.R.id.text2},
                0
        );
        setListAdapter(mAdapter);

        // Get the Loader
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Cursor c = (Cursor) getListView().getItemAtPosition(position);
        Capsule capsule = new CapsulePojo(c);
        if (capsule.getId() > 0 && mAccountName != null) {
            Intent intent = new Intent(getActivity(), CapsuleActivity.class);
            intent.putExtra("owned", mOwned);
            intent.putExtra("capsule", capsule);
            intent.putExtra("account_name", mAccountName);
            startActivityForResult(intent, REQUEST_CODE_CAPSULE_EDITOR);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = null;
        if (mAccountName != null) {
            loader = new CursorLoader(
                    getActivity(),
                    mUri,
                    mProjection,
                    mSelection,
                    mSelectionArgs,
                    null
            );
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
        c.setNotificationUri(getActivity().getContentResolver(), CapsuleContract.Capsules.CONTENT_URI);
        mAdapter.swapCursor(c);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

        case (REQUEST_CODE_CAPSULE_EDITOR) :
            if (resultCode == Activity.RESULT_OK) {
                // Check if a Capsule was modified
                mModified = data.getBooleanExtra("modified", false);
            }
            break;

        default:
            break;

        }

    }

    /**
     * Returns the flag indicating if a Capsule was modified.
     * 
     * @return
     */
    public boolean wasModified() {
        return mModified;
    }

}
