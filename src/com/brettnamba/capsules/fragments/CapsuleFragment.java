package com.brettnamba.capsules.fragments;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.brettnamba.capsules.R;
import com.brettnamba.capsules.dataaccess.Capsule;
import com.brettnamba.capsules.provider.CapsuleOperations;

/**
 * Fragment for displaying basic information about a Capsule.
 * 
 * @author Brett Namba
 *
 */
public class CapsuleFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_capsule, container, false);
        // Get the arguments passed in from the Activity
        Long id = getArguments().getLong("capsule_id");
        // Get a reference to the ProgressBar
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.fragment_capsule_progress_bar);
        // Begin the AsyncTask for loading the Capsule data
        new LoadCapsuleTask(getActivity(), progressBar).execute(id);
        return view;
    }

    /**
     * Queries the database for the Capsule record.
     */
    private class LoadCapsuleTask extends AsyncTask<Long, Void, Capsule> {

        /**
         * The Activity containing this Fragment.
         */
        private Activity activity;

        /**
         * ProgressBar to be displayed while the data is being loaded.
         */
        private ProgressBar progress;

        /**
         * Constructor
         * 
         * @param activity
         * @param progress
         */
        public LoadCapsuleTask(Activity activity, ProgressBar progress) {
            this.activity = activity;
            this.progress = progress;
        }

        @Override
        protected void onPreExecute() {
            this.progress.setIndeterminate(true);
            this.progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Capsule doInBackground(Long... params) {
            return CapsuleOperations.getCapsule(this.activity.getContentResolver(), params[0]);
        }

        @Override
        protected void onPostExecute(Capsule capsule) {
            if (this.progress.isShown()) {
                this.progress.setVisibility(View.GONE);
            }
            Toast.makeText(this.activity, "ID : " + capsule.getId() + ", NAME: " + capsule.getName(), Toast.LENGTH_SHORT).show();
        }

    }

}
