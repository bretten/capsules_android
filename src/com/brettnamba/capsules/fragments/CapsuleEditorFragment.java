package com.brettnamba.capsules.fragments;

import android.accounts.Account;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.location.Location;
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
import android.widget.Toast;

import com.brettnamba.capsules.R;
import com.brettnamba.capsules.dataaccess.Capsule;
import com.brettnamba.capsules.dataaccess.Memoir;
import com.brettnamba.capsules.http.RequestContract;
import com.brettnamba.capsules.services.SaveCapsuleService;
import com.brettnamba.capsules.util.Files;
import com.brettnamba.capsules.util.Images;
import com.brettnamba.capsules.util.Intents;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that provides the editor for a Capsule
 *
 * @author Brett Namba
 */
public class CapsuleEditorFragment extends Fragment implements
        OnMapReadyCallback,
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    /**
     * The Capsule being edited
     */
    private Capsule mCapsule;

    /**
     * The current Account
     */
    private Account mAccount;

    /**
     * The save button
     */
    private Button mSaveButton;

    /**
     * The MapView
     */
    private MapView mMapView;

    /**
     * The underlying GoogleMap
     */
    private GoogleMap mMap;

    /**
     * The Marker that indicates the current location
     */
    private Marker mLocationMarker;

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
     * The Google API client
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * Determines the accuracy of the location updates
     */
    private LocationRequest mLocationRequest;

    /**
     * The last Location that was retrieved
     */
    private Location mLastLocation;

    /**
     * Request code for starting an Activity to choose an upload
     */
    private static final int REQUEST_CODE_CHOOSE_UPLOAD = 1;

    /**
     * The interval in ms of the location request service
     */
    private static final int LOCATION_REQUEST_INTERVAL = 10000;

    /**
     * The default zoom level for the GoogleMap when focused on a location
     */
    private static final int ZOOM = 15;

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
            throw new ClassCastException(
                    activity.toString() + " does not implement CapsuleEditorFragmentListener");
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

        // Setup the services required for requesting the device's location
        this.setupLocationService();
    }

    /**
     * onCreateView
     *
     * @param inflater           The LayoutInflater used to inflate the layout View
     * @param container          The parent of the layout View
     * @param savedInstanceState State data if this Fragment is being recreated, otherwise null
     * @return The inflated layout View
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout View
        View view = inflater.inflate(R.layout.fragment_capsule_editor, container, false);

        // Get references to the Views and set up any listeners
        this.setupViews(view);

        // Forward onCreate to MapView
        this.mMapView.onCreate(savedInstanceState);

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
        if (this.mAccount == null) {
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
        // Check the state data
        if (savedInstanceState != null) {
            // See if the URI of the upload is in the state data
            if (savedInstanceState.containsKey("image_uri")) {
                // Get the URI from the state data
                this.mUploadUri = savedInstanceState.getParcelable("image_uri");
                // Preview the upload
                this.setUploadImageView(this.mUploadUri);
            }
            // See if a location was stored in the state data
            if (savedInstanceState.containsKey("location")) {
                // Get the location
                this.mLastLocation = savedInstanceState.getParcelable("location");
            }
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

        // Reconnect the LocationClient
        if (this.mGoogleApiClient != null && !this.mGoogleApiClient.isConnected()) {
            this.mGoogleApiClient.connect();
        }

        // Forward onResume to MapView
        this.mMapView.onResume();
    }

    /**
     * onPause
     */
    @Override
    public void onPause() {
        super.onPause();
        // Unregister the BroadcastReceiver
        this.getActivity().unregisterReceiver(this.mReceiver);

        // Disconnect from the Google Play services location API
        if (this.mGoogleApiClient != null && this.mGoogleApiClient.isConnected()) {
            // Remove location listener updates
            LocationServices.FusedLocationApi.removeLocationUpdates(this.mGoogleApiClient, this);
            // Disconnect
            this.mGoogleApiClient.disconnect();
        }

        // Forward onPause to MapView
        this.mMapView.onPause();
    }

    /**
     * onDestroy
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        // Forward onDestroy to MapView
        this.mMapView.onDestroy();
    }

    /**
     * onSaveInstanceState
     *
     * @param outState State data that will be passed to the replacement Fragment when it is
     *                 re-created
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the URI of the upload in the state data
        if (this.mUploadUri != null) {
            outState.putParcelable("image_uri", this.mUploadUri);
        }
        // Save the last location in the state data
        if (this.mLastLocation != null) {
            outState.putParcelable("location", this.mLastLocation);
        }

        // Forward onSaveInstanceState to MapView
        this.mMapView.onSaveInstanceState(outState);
    }

    /**
     * onLowMemory
     */
    @Override
    public void onLowMemory() {
        super.onLowMemory();

        // Forward onLowMemory to MapView
        this.mMapView.onLowMemory();
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
     * Called when the GoogleMap is ready
     *
     * @param googleMap The GoogleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;

        // If there is a previous location, update the location Marker and the center of the GoogleMap
        if (this.mLastLocation != null) {
            this.updateLocation(this.mLastLocation);
        }
    }

    /**
     * Called after the Google API has been connected to
     *
     * @param bundle Data provided by the Google API services
     */
    @Override
    public void onConnected(Bundle bundle) {
        // Request location updates
        LocationServices.FusedLocationApi
                .requestLocationUpdates(this.mGoogleApiClient, this.mLocationRequest, this);
    }

    /**
     * Called when the Google API connection has been suspended
     *
     * @param i The cause for the disconnection
     */
    @Override
    public void onConnectionSuspended(int i) {

    }

    /**
     * Called whenever the Location is updated
     *
     * @param location The updated Location
     */
    @Override
    public void onLocationChanged(Location location) {

    }

    /**
     * Called when a connection could not be established with the Google API service
     *
     * @param connectionResult A result indicating what caused the failure
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Notify the user the Google API services could not be reached
        Toast.makeText(this.getActivity(), this.getString(R.string.error_google_api_cannot_connect),
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Sets up the Capsule that will be edited
     */
    private void setupCapsule() {
        // Instantiate the Capsule if it has not already been
        if (this.mCapsule == null) {
            this.mCapsule = new Capsule();
        }
        if (this.mCapsule.getMemoir() == null) {
            this.mCapsule.setMemoir(new Memoir());
        }
    }

    /**
     * Sets the specified location on the Capsule being edited
     *
     * @param location The location
     */
    private void setLocationOnCapsule(Location location) {
        // Setup the Capsule
        this.setupCapsule();
        // Set the location
        this.mCapsule.setLatitude(location.getLatitude());
        this.mCapsule.setLongitude(location.getLongitude());
    }

    /**
     * Gets references to the Views in the layout View and sets up any listeners
     *
     * @param layout The layout View
     */
    private void setupViews(View layout) {
        // Get references to the EditTexts
        this.mCapsuleNameEditText = (EditText) layout.findViewById(
                R.id.fragment_capsule_editor_name);
        this.mMemoirTitleEditText = (EditText) layout.findViewById(
                R.id.fragment_capsule_editor_memoir_title);
        this.mMemoirMessageEditText = (EditText) layout.findViewById(
                R.id.fragment_capsule_editor_memoir_message);

        // Get a reference to the MapView
        this.mMapView = (MapView) layout.findViewById(R.id.fragment_capsule_editor_map);
        // Get the underlying GoogleMap
        this.mMapView.getMapAsync(this);

        // Set the location button listener
        Button locationButton =
                (Button) layout.findViewById(R.id.fragment_capsule_editor_location_button);
        locationButton.setOnClickListener(this.mLocationButtonListener);

        // Set the file chooser button listener
        Button fileChooserButton = (Button) layout.findViewById(
                R.id.fragment_capsule_editor_file_chooser_button);
        fileChooserButton.setOnClickListener(this.mFileButtonListener);

        // Get a reference to the upload preview ImageView
        this.mUploadImageView = (ImageView) layout.findViewById(
                R.id.fragment_capsule_editor_upload_image_view);

        // Set the save button listener
        this.mSaveButton = (Button) layout.findViewById(R.id.fragment_capsule_editor_save);
        this.mSaveButton.setOnClickListener(this.mSaveButtonListener);
    }

    /**
     * Maps the View values to the specified Capsule
     *
     * @param capsule The Capsule to map the View values to
     * @return The Capsule with the newly mapped data
     */
    private Capsule mapViewsToCapsule(Capsule capsule) {
        // Set the View values on the Capsule
        if (this.mCapsuleNameEditText != null) {
            capsule.setName(this.mCapsuleNameEditText.getText().toString().trim());
        }
        if (this.mMemoirTitleEditText != null) {
            capsule.getMemoir().setTitle(this.mMemoirTitleEditText.getText().toString().trim());
        }
        if (this.mMemoirMessageEditText != null) {
            capsule.getMemoir().setMessage(this.mMemoirMessageEditText.getText().toString().trim());
        }

        return capsule;
    }

    /**
     * Sets up everything required for accessing the device's Location
     */
    private void setupLocationService() {
        if (this.mGoogleApiClient == null) {
            this.mGoogleApiClient = new GoogleApiClient.Builder(this.getActivity())
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        if (this.mLocationRequest == null) {
            this.mLocationRequest = LocationRequest.create()
                    .setInterval(LOCATION_REQUEST_INTERVAL)
                    .setFastestInterval(LOCATION_REQUEST_INTERVAL)
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }
    }

    /**
     * Checks if the Capsule is valid
     *
     * @param capsule The Capsule being validated
     * @return True if it is valid, otherwise false
     */
    private boolean isValid(Capsule capsule) {
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
     * Updates the Fragment with the specified Location
     *
     * @param location The Location to update with
     */
    private void updateLocation(Location location) {
        // Keep track of the location
        this.mLastLocation = location;
        // Center the MapView on the location
        if (this.mMap != null) {
            // Zoom to the location
            this.mMap.moveCamera(CameraUpdateFactory
                    .newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()),
                            ZOOM));
            // Set the position of the location Marker
            this.setLocationMarkerPosition(this.mMap, location);
        }
        // Set the location data on the Capsule
        this.setLocationOnCapsule(location);
    }

    /**
     * Sets the position of the location Marker using the specified Location.  If the Marker has
     * not been instantiated, it will be added to the specified Map.
     *
     * @param map      The GoogleMap to add the Marker to if the Marker has not yet been
     *                 instantiated
     * @param location The new position of the Marker
     */
    private void setLocationMarkerPosition(GoogleMap map, Location location) {
        if (map == null || location == null) {
            return;
        }

        if (this.mLocationMarker != null) {
            this.mLocationMarker
                    .setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
        } else {
            this.mLocationMarker = map.addMarker(new MarkerOptions()
                    .position(new LatLng(location.getLatitude(), location.getLongitude()))
                    .draggable(false));
        }
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
     * Instantiates a new CapsuleEditorFragment instance given an Account
     *
     * @param account The current Account
     * @return The new instance of CapsuleEditorFragment
     */
    public static CapsuleEditorFragment createInstance(Account account) {
        CapsuleEditorFragment fragment = new CapsuleEditorFragment();

        // Bundle the Fragment arguments
        Bundle bundle = new Bundle();
        bundle.putParcelable("account", account);
        // Set the arguments on the Fragment
        fragment.setArguments(bundle);

        return fragment;
    }

    /**
     * Listener for the save button
     */
    private final OnClickListener mSaveButtonListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            // Setup the Capsule
            CapsuleEditorFragment.this.setupCapsule();
            // Map the View values to the Capsule
            CapsuleEditorFragment.this.mCapsule = CapsuleEditorFragment.this
                    .mapViewsToCapsule(CapsuleEditorFragment.this.mCapsule);
            CapsuleEditorFragment.this.mCapsule.getMemoir()
                    .setFileContentUri(CapsuleEditorFragment.this.mUploadUri);

            // Validate the Capsule
            if (CapsuleEditorFragment.this.isValid(CapsuleEditorFragment.this.mCapsule)) {
                // Disable the save button
                CapsuleEditorFragment.this.mSaveButton.setEnabled(false);
                // Save the Capsule on the background thread
                Intent saveIntent = new Intent(CapsuleEditorFragment.this.getActivity(),
                        SaveCapsuleService.class);
                saveIntent.putExtra("capsule", CapsuleEditorFragment.this.mCapsule);
                saveIntent.putExtra("account", CapsuleEditorFragment.this.mAccount);
                CapsuleEditorFragment.this.getActivity().startService(saveIntent);
            }
        }
    };

    /**
     * Click listener for the location update Button
     */
    private final OnClickListener mLocationButtonListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (CapsuleEditorFragment.this.mGoogleApiClient == null ||
                    !CapsuleEditorFragment.this.mGoogleApiClient.isConnected()) {
                return;
            }
            // Get the last location
            Location location = LocationServices.FusedLocationApi
                    .getLastLocation(CapsuleEditorFragment.this.mGoogleApiClient);
            if (location != null) {
                CapsuleEditorFragment.this.updateLocation(location);
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
                Capsule capsule = extras.getParcelable("capsule");
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
                        CapsuleEditorFragment.this, CapsuleEditorFragment.this.mCapsule,
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
        void onInvalidCapsuleData(CapsuleEditorFragment capsuleEditorFragment, Capsule capsule,
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
        void onSaveSuccess(CapsuleEditorFragment capsuleEditorFragment, Capsule capsule);

    }

}
