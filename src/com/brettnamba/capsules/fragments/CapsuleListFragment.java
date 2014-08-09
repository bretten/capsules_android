package com.brettnamba.capsules.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create the Adapter
        mAdapter = new SimpleCursorAdapter(
                getActivity(),
                android.R.layout.simple_list_item_2,
                null,
                new String[]{CapsuleContract.Capsules.NAME, CapsuleContract.Discoveries.ACCOUNT_NAME},
                new int[]{android.R.id.text1, android.R.id.text2},
                0
        );
        setListAdapter(mAdapter);

        // Get the Loader
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                CapsuleContract.Discoveries.CONTENT_URI,
                new String[]{CapsuleContract.Capsules.NAME, CapsuleContract.Discoveries.ACCOUNT_NAME},
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
        mAdapter.swapCursor(c);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

}
