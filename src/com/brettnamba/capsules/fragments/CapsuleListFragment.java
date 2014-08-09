package com.brettnamba.capsules.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.ListView;

import com.brettnamba.capsules.activities.CapsuleActivity;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get passed Bundle
        mAccountName = getArguments().getString("account_name");

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
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Cursor c = (Cursor) getListView().getItemAtPosition(position);
        long syncId = c.getLong(c.getColumnIndex(CapsuleContract.Capsules.SYNC_ID));
        if (syncId != 0 && mAccountName != null) {
            Intent intent = new Intent(getActivity(), CapsuleActivity.class);
            intent.putExtra("capsule_id", syncId);
            intent.putExtra("account_name", mAccountName);
            startActivity(intent);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = null;
        if (mAccountName != null) {
            loader = new CursorLoader(
                    getActivity(),
                    CapsuleContract.Discoveries.CONTENT_URI,
                    new String[]{CapsuleContract.Capsules.SYNC_ID, CapsuleContract.Capsules.NAME, CapsuleContract.Discoveries.ACCOUNT_NAME},
                    CapsuleContract.Discoveries.ACCOUNT_NAME + " = ?",
                    new String[]{mAccountName},
                    null
            );
        }
        return loader;
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
