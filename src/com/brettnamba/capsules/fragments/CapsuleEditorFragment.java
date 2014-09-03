package com.brettnamba.capsules.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.brettnamba.capsules.R;
import com.brettnamba.capsules.dataaccess.Capsule;
import com.brettnamba.capsules.dataaccess.CapsulePojo;
import com.brettnamba.capsules.provider.CapsuleContract;
import com.brettnamba.capsules.provider.CapsuleOperations;

/**
 * This Fragment provides a UI for editing a Capsule's data.
 * 
 * @author Brett Namba
 *
 */
public class CapsuleEditorFragment extends Fragment {

    /**
     * The current Account name.
     */
    private String mAccountName;

    /**
     * The Capsule being edited.
     */
    private Capsule mCapsule;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_capsule_editor, container, false);

        // Get passed Bundle
        mAccountName = getArguments().getString("account_name");
        mCapsule = getArguments().getParcelable("capsule");
        if (mCapsule == null) {
            mCapsule = new CapsulePojo();
            mCapsule.setLatitude(getArguments().getDouble("latitude"));
            mCapsule.setLongitude(getArguments().getDouble("longitude"));
        }

        // Populate the views if an existing Capsule is being edited
        if (mCapsule.getId() > 0) {
            EditText name = (EditText) view.findViewById(R.id.fragment_capsule_editor_name);
            name.setText(mCapsule.getName());
        }

        // Set the save button listener
        Button saveButton = (Button) view.findViewById(R.id.fragment_capsule_editor_save);
        saveButton.setOnClickListener(new SaveButtonListener(view));

        return view;
    }

    /**
     * Handler for clicking on the save button.
     */
    private class SaveButtonListener implements OnClickListener {

        /**
         * Reference to the root view for the save button.
         */
        private View rootView;

        /**
         * Constructor
         * 
         * @param rootView
         */
        public SaveButtonListener(View rootView) {
            this.rootView = rootView;
        }

        @Override
        public void onClick(View v) {
            // Get references to the inputs
            EditText name = (EditText) rootView.findViewById(R.id.fragment_capsule_editor_name);
            mCapsule.setName(name.getText().toString());
            new SaveCapsuleTask(getActivity(), rootView).execute(mCapsule);
        }

    }

    /**
     * Handles saving a Capsule to the database.
     */
    private class SaveCapsuleTask extends AsyncTask<Capsule, Void, Capsule> {

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

        public SaveCapsuleTask(Activity activity, View view) {
            this.activity = activity;
            this.view = view;
            this.progress = (ProgressBar) view.findViewById(R.id.fragment_capsule_editor_progress_bar);
        }

        @Override
        protected void onPreExecute() {
            this.progress.setIndeterminate(true);
            this.progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Capsule doInBackground(Capsule... params) {
            Capsule capsule = params[0];
            if (capsule.getId() <= 0) {
                Uri uri = CapsuleOperations.insertOwnership(this.activity.getContentResolver(), capsule, mAccountName, true /* setDirty */);
                long capsuleId = Long.valueOf(uri.getQueryParameter(CapsuleContract.Ownerships.CAPSULE_ID));
                if (capsuleId > 0) {
                    capsule.setId(capsuleId);
                }
            } else {
                CapsuleOperations.updateCapsule(this.activity.getContentResolver(), capsule, true /* setDirty */);
            }
            return capsule;
        }

        @Override
        protected void onPostExecute(final Capsule newCapsule) {
            if (this.progress.isShown()) {
                this.progress.setVisibility(View.GONE);
            }
            if (newCapsule != null) {
                Intent intent = new Intent();
                intent.putExtra("capsule", newCapsule);
                this.activity.setResult(Activity.RESULT_OK, intent);
                this.activity.finish();
            }
        }

    }

}
