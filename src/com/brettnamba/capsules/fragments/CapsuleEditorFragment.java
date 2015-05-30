package com.brettnamba.capsules.fragments;

import android.accounts.Account;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.brettnamba.capsules.R;
import com.brettnamba.capsules.dataaccess.CapsuleOwnership;
import com.brettnamba.capsules.os.AsyncListenerTask;
import com.brettnamba.capsules.os.OwnershipSaveTask;
import com.brettnamba.capsules.provider.CapsuleContract;
import com.brettnamba.capsules.provider.CapsuleOperations;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that provides the editor for a Capsule
 *
 * @author Brett Namba
 */
public class CapsuleEditorFragment extends Fragment implements
        AsyncListenerTask.OwnershipSaveTaskListener {

    /**
     * The Capsule being edited
     */
    private CapsuleOwnership mCapsule;

    /**
     * Temporary Capsule that will hold the edited values until the save is a success
     */
    private CapsuleOwnership mCapsuleClone;

    /**
     * The current Account
     */
    private Account mAccount;

    /**
     * The save button
     */
    private Button mSaveButton;

    /**
     * The name input
     */
    private EditText mNameEditText;

    /**
     * Listener that handles callbacks
     */
    private CapsuleEditorFragmentListener mListener;

    /**
     * onAttach
     *
     * @param activity The Activity the Fragment is being attached to
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Make sure the Activity implements this Fragment's listener interface
        try {
            this.mListener = (CapsuleEditorFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " does not implement CapsuleEditorFragmentListener");
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
            this.mCapsule = args.getParcelable("capsule");
            this.mAccount = args.getParcelable("account");
        }
    }

    /**
     * onCreateView
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout View
        View view = inflater.inflate(R.layout.fragment_capsule_editor, container, false);

        // Populate the Views
        if (this.mCapsule != null && this.mAccount != null) {
            // Clone the Capsule
            this.mCapsuleClone = new CapsuleOwnership(this.mCapsule);
            // Capsule name
            this.mNameEditText = (EditText) view.findViewById(R.id.fragment_capsule_editor_name);
            if (this.mCapsule.getName() != null) {
                this.mNameEditText.setText(this.mCapsule.getName());
            }
        }

        // Set the save button listener
        this.mSaveButton = (Button) view.findViewById(R.id.fragment_capsule_editor_save);
        this.mSaveButton.setOnClickListener(this.mSaveButtonListener);

        return view;
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
        if (this.mCapsule == null || this.mAccount == null) {
            this.mListener.onMissingData(this);
        }
    }

    /**
     * Saves the Capsule on the background thread
     *
     * @param params The Capsule being saved
     * @return The result of the save
     */
    @Override
    public boolean duringOwnershipSave(CapsuleOwnership... params) {
        return CapsuleOperations.Ownerships.save(this.getActivity().getContentResolver(),
                params[0], CapsuleContract.SyncStateAction.DIRTY);
    }

    /**
     * Disables certain parts of the UI on the UI thread before the Capsule is saved on the background
     * thread
     */
    @Override
    public void onPreOwnershipSave() {
        this.mSaveButton.setEnabled(false);
    }

    /**
     * If the save was a success, work is delegated to the listener, otherwise the UI
     * is re-enabled
     *
     * @param success The result of the save
     */
    @Override
    public void onPostOwnershipSave(Boolean success) {
        if (success != null && success && this.mListener != null) {
            this.mCapsule = this.mCapsuleClone;
            // Delegates the handling of the success to the listener
            this.mListener.onSaveSuccess(this, this.mCapsule);
        } else {
            this.mSaveButton.setEnabled(true);
        }
    }

    /**
     * Re-enables the UI on the UI thread if the Capsule save background task was cancelled
     */
    @Override
    public void onOwnershipSaveCancelled() {
        this.mSaveButton.setEnabled(true);
    }

    /**
     * Checks if the Capsule is valid
     *
     * @param capsule
     * @return
     */
    private boolean isValid(CapsuleOwnership capsule) {
        // Will hold any errors from validation
        List<String> errors = new ArrayList<String>();

        if (capsule == null) {
            errors.add(this.getString(R.string.validation_capsule_missing));
        } else {
            // Capsule name
            if (capsule.getName() == null || TextUtils.isEmpty(capsule.getName())) {
                errors.add(this.getString(R.string.validation_capsule_name));
            }
        }

        boolean isValid = errors.isEmpty();
        if (!isValid) {
            // Delegate the validation errors to the listener
            this.mListener.onInvalidCapsuleData(this, capsule, errors);
        }
        return isValid;
    }

    /**
     * Listener for the save button
     */
    private final OnClickListener mSaveButtonListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            // Capsule name
            CapsuleEditorFragment.this.mCapsuleClone.setName(
                    CapsuleEditorFragment.this.mNameEditText.getText().toString()
            );
            if (CapsuleEditorFragment.this.isValid(CapsuleEditorFragment.this.mCapsuleClone)) {
                // Save the Capsule on the background thread
                new OwnershipSaveTask(CapsuleEditorFragment.this).execute(CapsuleEditorFragment.this.mCapsuleClone);
            }
        }
    };

    /**
     * Listener that provides event callbacks for this Fragment
     */
    public interface CapsuleEditorFragmentListener {

        /**
         * Should handle the case where the Fragment is not passed the required data
         *
         * @param capsuleEditorFragment The Fragment that is missing the data
         */
        void onMissingData(CapsuleEditorFragment capsuleEditorFragment);

        /**
         * Should handle the case when the Fragment tried to save invalid Capsule data
         *
         * @param capsuleEditorFragment The Fragment that attempted the save
         * @param capsule               The Capsule with invalid data
         * @param messages              The error messages
         */
        void onInvalidCapsuleData(CapsuleEditorFragment capsuleEditorFragment, CapsuleOwnership capsule,
                                  List<String> messages);

        /**
         * Should handle the case where the Fragment has successfully saved the Capsule data
         *
         * @param capsuleEditorFragment The Fragment that saved the data
         * @param capsule               The Capsule that was used to save
         */
        void onSaveSuccess(CapsuleEditorFragment capsuleEditorFragment, CapsuleOwnership capsule);

    }

}
