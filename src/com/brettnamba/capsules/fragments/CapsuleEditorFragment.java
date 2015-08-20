package com.brettnamba.capsules.fragments;

import android.accounts.Account;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.brettnamba.capsules.R;
import com.brettnamba.capsules.dataaccess.CapsuleOwnership;
import com.brettnamba.capsules.dataaccess.Memoir;
import com.brettnamba.capsules.http.RequestContract;
import com.brettnamba.capsules.services.SaveCapsuleService;
import com.brettnamba.capsules.util.Files;
import com.brettnamba.capsules.util.Images;
import com.brettnamba.capsules.util.Intents;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that provides the editor for a Capsule
 *
 * @author Brett Namba
 */
public class CapsuleEditorFragment extends Fragment {

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
     * The Capsule name input
     */
    private EditText mCapsuleNameEditText;

    /**
     * The Memoir title input
     */
    private EditText mMemoirTitleEditText;

    /**
     * The Memoir message input
     */
    private EditText mMemoirMessageEditText;

    /**
     * URI of the file upload
     */
    private Uri mUploadUri;

    /**
     * ImageView that shows a preview of the upload
     */
    private ImageView mUploadImageView;

    /**
     * Listener that handles callbacks
     */
    private CapsuleEditorFragmentListener mListener;

    /**
     * The BroadcastReceiver for listening for updates from SaveCapsuleService
     */
    private SaveCapsuleReceiver mReceiver;

    /**
     * Request code for starting an Activity to choose an upload
     */
    private static final int REQUEST_CODE_CHOOSE_UPLOAD = 1;

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
     * @param savedInstanceState State data if this Fragment is being recreated, otherwise null
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
     * @param inflater           The LayoutInflater used to inflate the layout View
     * @param container          The parent of the layout View
     * @param savedInstanceState State data if this Fragment is being recreated, otherwise null
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout View
        View view = inflater.inflate(R.layout.fragment_capsule_editor, container, false);

        // Get references to the EditTexts
        this.mCapsuleNameEditText = (EditText) view.findViewById(
                R.id.fragment_capsule_editor_name);
        this.mMemoirTitleEditText = (EditText) view.findViewById(
                R.id.fragment_capsule_editor_memoir_title);
        this.mMemoirMessageEditText = (EditText) view.findViewById(
                R.id.fragment_capsule_editor_memoir_message);

        // Populate the Views
        if (this.mCapsule != null && this.mAccount != null) {
            // Clone the Capsule
            this.mCapsuleClone = new CapsuleOwnership(this.mCapsule);
            // Capsule name
            if (this.mCapsule.getName() != null) {
                this.mCapsuleNameEditText.setText(this.mCapsule.getName());
            }
            // Set the Memoir
            Memoir memoir = this.mCapsule.getMemoir();
            if (memoir != null) {
                // Set the Memoir title
                if (memoir.getTitle() != null) {
                    this.mMemoirTitleEditText.setText(memoir.getTitle());
                }
                // Set the Memoir message
                if (memoir.getMessage() != null) {
                    this.mMemoirMessageEditText.setText(memoir.getMessage());
                }
            }
        }

        // Set the file chooser button listener
        Button fileChooserButton = (Button) view.findViewById(
                R.id.fragment_capsule_editor_file_chooser_button);
        fileChooserButton.setOnClickListener(this.mFileButtonListener);

        // Get a reference to the upload preview ImageView
        this.mUploadImageView = (ImageView) view.findViewById(
                R.id.fragment_capsule_editor_upload_image_view);

        // Set the save button listener
        this.mSaveButton = (Button) view.findViewById(R.id.fragment_capsule_editor_save);
        this.mSaveButton.setOnClickListener(this.mSaveButtonListener);

        return view;
    }

    /**
     * onActivityCreated
     *
     * @param savedInstanceState State data if this Fragment is being recreated, otherwise null
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
     * onViewStateRestored
     *
     * @param savedInstanceState State data if this Fragment is being recreated, otherwise null
     */
    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        // See if the URI of the upload is in the state data
        if (savedInstanceState != null && savedInstanceState.containsKey("image_uri")) {
            // Get the URI from the state data
            this.mUploadUri = savedInstanceState.getParcelable("image_uri");
            // Preview the upload
            this.setUploadImageView(this.mUploadUri);
        }
    }

    /**
     * onResume
     */
    @Override
    public void onResume() {
        super.onResume();
        // Register with the BroadcastReceiver
        this.mReceiver = new SaveCapsuleReceiver();
        IntentFilter intentFilter = new IntentFilter(SaveCapsuleService.BROADCAST_ACTION);
        this.getActivity().registerReceiver(this.mReceiver, intentFilter);
    }

    /**
     * onPause
     */
    @Override
    public void onPause() {
        super.onPause();
        // Unregister the BroadcastReceiver
        this.getActivity().unregisterReceiver(this.mReceiver);
    }

    /**
     * onSaveInstanceState
     *
     * @param outState State data that will be passed to the replacement Fragment when it is re-created
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the URI of the upload in the state data
        if (this.mUploadUri != null) {
            outState.putParcelable("image_uri", this.mUploadUri);
        }
    }

    /**
     * onActivityResult
     *
     * @param requestCode The integer code that was used to start the Activity
     * @param resultCode  The integer code that was returned by the Activity
     * @param data        Intent with result data from the Activity
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check the result code
        if (resultCode == Activity.RESULT_OK) {
            // Check the request code
            if (requestCode == CapsuleEditorFragment.REQUEST_CODE_CHOOSE_UPLOAD) {
                // Determine if the upload came from the device's camera
                final boolean fromCamera = Intents.isActivityResultIntentFromCamera(data);

                // Vary action based on if the upload is from the camera or gallery
                if (fromCamera) {
                    // The upload came from the camera, so make sure the URI member has been populated
                    if (this.mUploadUri != null) {
                        // Notify the Media Provider a new image has been taken
                        Intents.notifyMediaProviderOfNewImage(this.getActivity(), this.mUploadUri);
                    }
                } else {
                    // The upload came from the gallery, so make sure the URI was passed in the Intent
                    if (data.getData() != null) {
                        // Keep a reference to the upload URI
                        this.mUploadUri = data.getData();
                    }
                }
                // Show a preview of the upload
                this.setUploadImageView(this.mUploadUri);
            }
        }
    }

    /**
     * Checks if the Capsule is valid
     *
     * @param capsule The Capsule being validated
     * @return True if it is valid, otherwise false
     */
    private boolean isValid(CapsuleOwnership capsule) {
        // Will hold any errors from validation
        List<String> errors = new ArrayList<String>();

        // Validate the Capsule
        if (capsule == null) {
            errors.add(this.getString(R.string.validation_capsule_missing));
        } else {
            // Capsule name
            if (capsule.getName() == null || TextUtils.isEmpty(capsule.getName())) {
                errors.add(this.getString(R.string.validation_capsule_name));
            }

            // Validate the Memoir
            Memoir memoir = capsule.getMemoir();
            if (memoir == null) {
                errors.add(this.getString(R.string.validation_memoir_missing));
            } else {
                // Memoir title
                if (memoir.getTitle() == null || TextUtils.isEmpty(memoir.getTitle())) {
                    errors.add(this.getString(R.string.validation_memoir_missing_title));
                }
                // Memoir file content URI
                if (memoir.getFileContentUri() == null) {
                    errors.add(this.getString(R.string.validation_upload_missing));
                }
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
     * Sets the upload preview ImageView with the image that exists at the specified URI.
     *
     * @param uploadUri The file or content URI where the upload is located
     */
    private void setUploadImageView(Uri uploadUri) {
        // Will hold any errors
        List<String> errors = new ArrayList<String>();

        // Make sure the View and upload URI exist
        if (this.mUploadImageView != null && uploadUri != null) {
            try {
                // Check the file size in bytes
                long size = Files.getFileSize(this.getActivity().getApplicationContext(),
                        uploadUri);
                if (size > RequestContract.Upload.MAX_IMAGE_FILE_SIZE) {
                    // Display an error message indicating the file exceeds the limit
                    errors.add(this.getString(R.string.validation_upload_exceeds_size_limit)
                            + RequestContract.Upload.MAX_IMAGE_FILE_SIZE_HUMAN);
                } else if (size <= 0) {
                    // Display an error message indicating the file content was empty
                    errors.add(this.getString(R.string.error_upload_file_content_empty));
                } else {
                    // Get a Bitmap preview of the upload
                    Bitmap bitmap = Images.getImageFromUri(this.getActivity(), uploadUri);
                    // Set the Bitmap on the ImageView
                    this.mUploadImageView.setImageBitmap(Images.scaleBitmap(
                            this.getActivity(), bitmap, /* widthScaleFactor */ 0.5));
                }
            } catch (FileNotFoundException e) {
                // The file could not be found
                errors.add(this.getString(R.string.error_upload_file_not_found));
            }
        } else {
            // The file could not be retrieved from the gallery or camera
            errors.add(this.getString(R.string.error_upload_cannot_process));
        }

        // Display the errors
        if (this.mListener != null && !errors.isEmpty()) {
            // Clear out the upload URI
            this.mUploadUri = null;
            // Delegate handling to the listener
            this.mListener.onInvalidCapsuleData(this, this.mCapsule, errors);
        }
    }

    /**
     * Listener for the save button
     */
    private final OnClickListener mSaveButtonListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            // Set the Capsule name
            CapsuleEditorFragment.this.mCapsuleClone.setName(
                    CapsuleEditorFragment.this.mCapsuleNameEditText.getText().toString()
            );
            // Instantiate new Memoir
            Memoir memoir = new Memoir();
            // Set the Memoir title
            memoir.setTitle(CapsuleEditorFragment.this.mMemoirTitleEditText.getText().toString());
            // Set the Memoir message
            memoir.setMessage(
                    CapsuleEditorFragment.this.mMemoirMessageEditText.getText().toString());
            // Set the file content URI
            memoir.setFileContentUri(CapsuleEditorFragment.this.mUploadUri);
            // Set the Memoir on the Capsule
            CapsuleEditorFragment.this.mCapsuleClone.setMemoir(memoir);

            // Validate the Capsule
            if (CapsuleEditorFragment.this.isValid(CapsuleEditorFragment.this.mCapsuleClone)) {
                // Disable the save button
                CapsuleEditorFragment.this.mSaveButton.setEnabled(false);
                // Save the Capsule on the background thread
                Intent saveIntent = new Intent(CapsuleEditorFragment.this.getActivity(),
                        SaveCapsuleService.class);
                saveIntent.putExtra("capsule", CapsuleEditorFragment.this.mCapsuleClone);
                saveIntent.putExtra("account", CapsuleEditorFragment.this.mAccount);
                CapsuleEditorFragment.this.getActivity().startService(saveIntent);
            }
        }
    };

    /**
     * Listener for the file button
     */
    private final OnClickListener mFileButtonListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            // Get an Intent that allows for choosing between the camera and the gallery
            Pair<Intent, Uri> intentUriPair = Intents.getCameraAndGalleryIntentChooser(
                    CapsuleEditorFragment.this.getActivity());
            // Get the Intent and the camera file URI (will be null if the camera was not used)
            Intent chooseIntent = intentUriPair.first;
            CapsuleEditorFragment.this.mUploadUri = intentUriPair.second;
            // Start the Activity
            CapsuleEditorFragment.this.startActivityForResult(chooseIntent,
                    CapsuleEditorFragment.REQUEST_CODE_CHOOSE_UPLOAD);
            // Execute the listener's handler for the event of choosing the upload source
            if (CapsuleEditorFragment.this.mListener != null) {
                CapsuleEditorFragment.this.mListener.onChooseUploadSource(
                        CapsuleEditorFragment.this);
            }
            // Reset the ImageView
            CapsuleEditorFragment.this.mUploadImageView.setImageResource(
                    android.R.color.transparent);
        }
    };

    /**
     * BroadcastReceiver for listening for updates from SaveCapsuleService
     */
    private class SaveCapsuleReceiver extends BroadcastReceiver {

        /**
         * onReceive
         *
         * @param context The Context running the BroadcastReceiver
         * @param intent  The Intent that was broadcast from the Service
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            // Make sure the Intent is not null
            if (intent == null) {
                return;
            }
            // Make sure the Intent has extras
            Bundle extras = intent.getExtras();
            if (extras == null) {
                return;
            }
            // Check if there was a Capsule object
            if (extras.containsKey("capsule")) {
                CapsuleOwnership capsule = extras.getParcelable("capsule");
                if (capsule != null) {
                    if (CapsuleEditorFragment.this.mListener != null) {
                        // Delegates the handling of the success to the listener
                        CapsuleEditorFragment.this.mListener.onSaveSuccess(
                                CapsuleEditorFragment.this, capsule);
                    } else {
                        // Close the Activity for editing Capsules
                        CapsuleEditorFragment.this.getActivity().finish();
                    }
                }
            } else if (extras.containsKey("messages")) {
                // There were messages broadcast from the Service
                List<String> messages = extras.getStringArrayList("messages");
                // Delegate the messages to the listener
                CapsuleEditorFragment.this.mListener.onInvalidCapsuleData(
                        CapsuleEditorFragment.this, CapsuleEditorFragment.this.mCapsuleClone,
                        messages);
            }

            // Re-enable the save button
            CapsuleEditorFragment.this.mSaveButton.setEnabled(true);
        }

    }

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
         * Should handle the case when the Fragment triggers the file upload chooser
         *
         * @param capsuleEditorFragment The Fragment that is choosing the upload source
         */
        void onChooseUploadSource(CapsuleEditorFragment capsuleEditorFragment);

        /**
         * Should handle the case where the Fragment has successfully saved the Capsule data
         *
         * @param capsuleEditorFragment The Fragment that saved the data
         * @param capsule               The Capsule that was used to save
         */
        void onSaveSuccess(CapsuleEditorFragment capsuleEditorFragment, CapsuleOwnership capsule);

    }

}
