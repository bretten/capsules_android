package com.brettnamba.capsules.fragments;

import android.app.Activity;
import android.content.ContentValues;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.brettnamba.capsules.R;
import com.brettnamba.capsules.dataaccess.Discovery;
import com.brettnamba.capsules.provider.CapsuleContract;
import com.brettnamba.capsules.provider.CapsuleOperations;

/**
 * Displays Discovery data for a Capsule and the current Account.
 * 
 * @author Brett
 *
 */
public class DiscoveryFragment extends Fragment {

    /**
     * The Capsule id
     */
    private long mCapsuleId;

    /**
     * The Account name
     */
    private String mAccountName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get the arguments passed in from the Activity
        this.mCapsuleId = getArguments().getLong("capsule_id");
        this.mAccountName = getArguments().getString("account_name");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discovery, container, false);
        // Begin the AsyncTask for loading the Capsule data
        new LoadDiscoveryTask(getActivity(), view).execute(String.valueOf(this.mCapsuleId), this.mAccountName);
        return view;
    }

    /**
     * Handles changes to the favorite toggle.
     */
    private class FavoriteListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            ContentValues values = new ContentValues();
            values.put(CapsuleContract.Discoveries.FAVORITE, (isChecked) ? 1 : 0);
            new UpdateDiscoveryTask(getActivity(), getView(), values).execute(String.valueOf(mCapsuleId), mAccountName);
        }

    }

    /**
     * Handles changes to the rating input.
     */
    private class RatingListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            ContentValues values = new ContentValues();
            values.put(CapsuleContract.Discoveries.RATING, Integer.valueOf((String) parent.getItemAtPosition(pos)));
            new UpdateDiscoveryTask(getActivity(), getView(), values).execute(String.valueOf(mCapsuleId), mAccountName);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            parent.setSelection(0);
        }

    }

    /**
     * Queries the database for a Discovery row.
     */
    private class LoadDiscoveryTask extends AsyncTask<String, Void, Discovery> {

        /**
         * The Activity containing this Fragment.
         */
        private Activity activity;

        /**
         * The View for the Fragment.
         */
        private View view;

        /**
         * ProgressBar to be displayed while the data is being loaded.
         */
        private ProgressBar progress;

        /**
         * Constructor
         * 
         * @param activity
         * @param view
         */
        public LoadDiscoveryTask(Activity activity, View view) {
            this.activity = activity;
            this.view = view;
            this.progress = (ProgressBar) view.findViewById(R.id.fragment_discovery_progress_bar);
        }

        @Override
        protected void onPreExecute() {
            this.progress.setIndeterminate(true);
            this.progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Discovery doInBackground(String... params) {
            return CapsuleOperations.getDiscovery(this.activity.getContentResolver(), Long.valueOf(params[0]), params[1]);
        }

        @Override
        protected void onPostExecute(final Discovery discovery) {
            if (this.progress.isShown()) {
                this.progress.setVisibility(View.GONE);
            }
            // Set up the favorite toggle
            ToggleButton favoriteToggle = (ToggleButton) this.view.findViewById(R.id.fragment_discovery_favorite);
            favoriteToggle.setOnCheckedChangeListener(new FavoriteListener());
            favoriteToggle.setChecked((discovery.getFavorite() > 0) ? true : false);
            favoriteToggle.setVisibility(View.VISIBLE);
            // Set up the rating input
            Spinner ratingSpinner = (Spinner) this.view.findViewById(R.id.fragment_discovery_rating);
            String[] ratings = new String[]{"0", "-1", "1"};
            ArrayAdapter<String> ratingsArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, ratings);
            ratingsArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            ratingSpinner.setAdapter(ratingsArrayAdapter);
            ratingSpinner.setOnItemSelectedListener(new RatingListener());
            ratingSpinner.setSelection(ratingsArrayAdapter.getPosition(String.valueOf(discovery.getRating())));
            ratingSpinner.setVisibility(View.VISIBLE);
        }

    }

    /**
     * Updates the Discovery row in the database.
     */
    private class UpdateDiscoveryTask extends AsyncTask<String, Void, Boolean> {

        /**
         * The Activity containing this Fragment.
         */
        private Activity activity;

        /**
         * ProgressBar to be displayed while the data is being loaded.
         */
        private ProgressBar progress;

        /**
         * The ContentValues being used in the UPDATE statement.
         */
        private ContentValues values;

        /**
         * Constructor
         * 
         * @param activity
         * @param view
         */
        public UpdateDiscoveryTask(Activity activity, View view, ContentValues values) {
            this.activity = activity;
            this.progress = (ProgressBar) view.findViewById(R.id.fragment_discovery_progress_bar);
            this.values = values;
        }

        @Override
        protected void onPreExecute() {
            this.progress.setIndeterminate(true);
            this.progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            return CapsuleOperations.updateDiscovery(this.activity.getContentResolver(), this.values, Long.valueOf(params[0]), params[1]);
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (this.progress.isShown()) {
                this.progress.setVisibility(View.GONE);
            }
            if (success) {
                Toast.makeText(this.activity, "Updated successfully.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this.activity, "There was a problem during the save.", Toast.LENGTH_SHORT).show();
            }
        }

    }

}
