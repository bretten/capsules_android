package com.brettnamba.capsules;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.brettnamba.capsules.activities.CapsuleActivity;
import com.brettnamba.capsules.activities.CapsuleEditorActivity;
import com.brettnamba.capsules.activities.CapsuleListActivity;
import com.brettnamba.capsules.authenticator.AccountDialogFragment;
import com.brettnamba.capsules.dataaccess.Capsule;
import com.brettnamba.capsules.dataaccess.CapsulePojo;
import com.brettnamba.capsules.fragments.NavigationDrawerFragment;
import com.brettnamba.capsules.fragments.RetainedMapFragment;
import com.brettnamba.capsules.http.HttpFactory;
import com.brettnamba.capsules.http.RequestHandler;
import com.brettnamba.capsules.http.response.CapsulePingResponse;
import com.brettnamba.capsules.os.AsyncListenerTask;
import com.brettnamba.capsules.provider.CapsuleContract;
import com.brettnamba.capsules.provider.CapsuleOperations;
import com.brettnamba.capsules.util.Accounts;
import com.brettnamba.capsules.util.JSONParser;
import com.brettnamba.capsules.widget.NavigationDrawerItem;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.json.JSONException;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The main Activity that displays a GoogleMap and capsules to the user.
 * 
 * @author Brett
 *
 */
public class MainActivity extends FragmentActivity implements
        OnMapReadyCallback,
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        AccountDialogFragment.AccountDialogListener,
        NavigationDrawerFragment.NavigationDrawerListener,
        AsyncListenerTask.AuthTokenRetrievalTaskListener,
        AsyncListenerTask.CapsulePingTaskListener {

    /**
     * Fragment used to retain the state of the Map and the user
     */
    private RetainedMapFragment mRetainedFragment;

    /**
     * Reference to the GoogleMap.
     */
    private GoogleMap mMap;

    /**
     * Reference to the Circle that surrounds the user's location, designating their "discovery" radius.
     */
    private Circle mUserCircle;

    /**
     * Reference to the GoogleApiClient.
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * Reference to the AccountManager.
     */
    private AccountManager mAccountManager;

    /**
     * Reference to the current Account.
     */
    private Account mAccount;

    /**
     * Reference to the authentication token.
     */
    private String mAuthToken;

    /**
     * Reference to the HttpClient.
     */
    private HttpClient mHttpClient;

    /**
     * Reference to the HTTP RequestHandler.
     */
    private RequestHandler mRequestHandler;

    /**
     * Navigation DrawerLayout
     */
    private DrawerLayout mDrawerLayout;

    /**
     * The View for the navigation drawer
     */
    private View mDrawerView;

    /**
     * The navigation drawer Fragment
     */
    private NavigationDrawerFragment mDrawerFragment;

    /**
     * Mapping of undiscovered Capsule sync id's to Marker.
     */
    private Map<Marker, Capsule> mUndiscoveredMarkers;

    /**
     * Mapping of all discovered Capsule to Marker.
     */
    private Map<Marker, Capsule> mDiscoveredMarkers;

    /**
     * Mapping of all owned Capsules to Markers.
     */
    private Map<Marker, Capsule> mOwnedMarkers;

    /**
     * Holds the Marker that is used to create new Capsules.
     */
    private Marker mNewCapsuleMarker;

    /**
     * Flag to determine if the map needs centering on the user's location during onCreate or onResume.
     */
    private boolean mNeedsCentering = true;

    /**
     * The radius around the user's location in which capsules can be opened (meters).
     */
    private static final int DISCOVERY_RADIUS = 161;

    /**
     * The color of the user's location circle.
     */
    private static final int USER_CIRCLE_COLOR = Color.argb(60, 153, 204, 0);

    /**
     * The default zoom level.
     */
    private static final int ZOOM = 15;

    /**
     * Quality of Location service settings.
     */
    private static final LocationRequest REQUEST = LocationRequest.create()
            .setInterval(10000)
            .setFastestInterval(5000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    /**
     * Request code for CapsuleActivity
     */
    private static final int REQUEST_CODE_CAPSULE = 1;

    /**
     * Request code for CapsuleEditorActivity (creating a new Capsule)
     */
    private static final int REQUEST_CODE_CAPSULE_EDITOR = 2;

    /**
     * Request code for CapsuleListActivity
     */
    private static final int REQUEST_CODE_CAPSULE_LIST = 3;

    /**
     * Tag for the Fragment retaining state about this Activity
     */
    private static final String RETAINED_MAP_FRAGMENT_TAG = "retained_map";

    /**
     * The tag used for logging.
     */
    private static final String TAG = "MainActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Fragment manager
        FragmentManager fragmentManager = this.getSupportFragmentManager();

        // If the Activity is being recreated, see if the retained already Fragment exists
        this.mRetainedFragment = ((RetainedMapFragment) fragmentManager.findFragmentByTag(RETAINED_MAP_FRAGMENT_TAG));
        // If the Activity is being created for the first time, create the retainer Fragments
        if (this.mRetainedFragment == null) {
            this.mRetainedFragment = new RetainedMapFragment();
            fragmentManager.beginTransaction().add(this.mRetainedFragment, RETAINED_MAP_FRAGMENT_TAG).commit();
        }

        // Set up the GoogleMap and LocationClient
        SupportMapFragment mapFragment = ((SupportMapFragment) fragmentManager.findFragmentById(R.id.map));
        mapFragment.getMapAsync(this);
        this.setLocationClient();

        // AccountManager
        this.mAccountManager = AccountManager.get(this);

        // HTTP client and request handler
        mHttpClient = HttpFactory.getInstance();
        mRequestHandler = new RequestHandler(mHttpClient);

        // Navigation Drawer
        this.mDrawerFragment = (NavigationDrawerFragment) fragmentManager.findFragmentById(R.id.navigation_drawer);
        this.mDrawerLayout = (DrawerLayout) this.findViewById(R.id.drawer_layout);
        this.mDrawerView = this.findViewById(R.id.navigation_drawer);

        // Setup buttons to place over the GoogleMap
        ImageView drawerButton = (ImageView) this.findViewById(R.id.map_drawer_button);
        drawerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.mDrawerLayout.openDrawer(MainActivity.this.mDrawerView);
            }
        });
	}

	@Override
	protected void onResume() {
	    Log.i(TAG, "onResume()");
	    super.onResume();

        // Switch to the last used or default Account if there is one
        Account lastUsedAccount = Accounts.getLastUsedOrFirstAccount(this);
        if (lastUsedAccount != null) {
            this.switchAccount(lastUsedAccount);
        }

	    // Reconnect the LocationClient
	    if (mGoogleApiClient != null && !mGoogleApiClient.isConnected()) {
	        mGoogleApiClient.connect();
	    }
	}

	@Override
	public void onPause() {
	    Log.i(TAG, "onPause()");
	    super.onPause();

        // Store the last used Account
        if (this.mAccount != null) {
            Accounts.setLastUsedAccount(this, this.mAccount);
            // Retain the Account
            if (this.mRetainedFragment != null) {
                this.mRetainedFragment.setAccount(this.mAccount);
            }
        }

        // Disconnect the LocationClient
	    if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
	        mGoogleApiClient.disconnect();
	    }
	    // To re-center the map onResume(), set the flag
	    mNeedsCentering = true;
	}

    @Override
    protected void onStop() {
        Log.i(TAG, "onStop()");
        super.onStop();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (this.mRetainedFragment != null && !this.isChangingConfigurations()) {
                Log.i(TAG, "onStop: cancelling tasks");
                this.mRetainedFragment.cancelTasks();
            }
        } else {
            if (this.mRetainedFragment != null) {
                Log.i(TAG, "onStop: cancelling tasks");
                this.mRetainedFragment.cancelTasks();
            }
        }
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    Log.i(TAG, "onCreateOptionsMenu()");
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    Log.i(TAG, "onOptionsItemSelected()");
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		} else if (id == R.id.action_collection) {
            Intent intent = new Intent(getApplicationContext(), CapsuleListActivity.class);
            intent.putExtra("account_name", mAccount.name);
            startActivityForResult(intent, REQUEST_CODE_CAPSULE_LIST);
		} else if (item.isCheckable()) {
		    item.setChecked(!item.isChecked());
		    // The toggle for displaying capsules created by the user
		    if (id == R.id.action_created) {
		        if (item.isChecked()) {
		            // TODO Add created capsule markers
		        } else {
		            // TODO Remove created capsule markers
		        }
		    }
		    // The toggle for displaying the user's discovered capsules
		    if (id == R.id.action_discovered) {
		        if (item.isChecked()) {
                    for (Marker marker : mDiscoveredMarkers.keySet()) {
                        marker.setVisible(true);
                    }
		        } else {
		            for (Marker marker : mDiscoveredMarkers.keySet()) {
		                marker.setVisible(false);
		            }
		        }
		    }
		    // The toggle for showing satellite view
		    if (id == R.id.action_satellite) {
	            if (item.isChecked()) {
	                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
	            } else {
	                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
	            }
		    }
		}
		return super.onOptionsItemSelected(item);
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

        case (REQUEST_CODE_CAPSULE) :
            if (resultCode == Activity.RESULT_OK) {
                Capsule capsule = (Capsule) data.getParcelableExtra("capsule");
                // Replace the old Capsule Marker
                if (mOwnedMarkers.containsValue(capsule)) {
                    for (Map.Entry<Marker, Capsule> entry : mOwnedMarkers.entrySet()) {
                        if (capsule.equals(entry.getValue())) {
                            entry.getKey().setTitle(capsule.getName());
                            // Hide and show the info window to refresh it
                            entry.getKey().hideInfoWindow();
                            entry.getKey().showInfoWindow();
                            mOwnedMarkers.put(entry.getKey(), capsule);
                        }
                    }
                }
            }
            break;

        case (REQUEST_CODE_CAPSULE_EDITOR) :
            if (resultCode == Activity.RESULT_OK) {
                Capsule capsule = (Capsule) data.getParcelableExtra("capsule");
                // Remove the new Capsule Marker
                if (mNewCapsuleMarker != null) {
                    mNewCapsuleMarker.remove();
                    mNewCapsuleMarker = null;
                }
                // Create a Marker for the new Capsule data
                Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(capsule.getLatitude(), capsule.getLongitude()))
                    .title(capsule.getName())
                    .draggable(false)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                );
                // Add the Marker to the Owned Capsule collection
                mOwnedMarkers.put(marker, capsule);
            }
            break;

        case (REQUEST_CODE_CAPSULE_LIST) :
            // Refresh the Capsule Markers if there was a modification
            boolean modified = data.getBooleanExtra("modified", false);
            // Re-populate the Markers if one was modified
            if (modified) {
                this.populateStoredMarkers();
            }
            break;

        default:
            break;

        }

    }

    @Override
    public void onMapReady(GoogleMap map) {
        Log.i(TAG, "onMapReady()");
        mMap = map;

        // Specify map settings
        mMap.setMyLocationEnabled(true);
        // Create the circle
        mUserCircle = mMap.addCircle(new CircleOptions()
                        .center(new LatLng(0, 0))
                        .radius(DISCOVERY_RADIUS)
                        .strokeWidth(0)
                        .fillColor(USER_CIRCLE_COLOR)
        );
        // Create the info window listener
        mMap.setOnInfoWindowClickListener(new InfoWindowListener());
        // Create the long click listener
        mMap.setOnMapLongClickListener(new MapLongClickListener());
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "onConnectionFailed()");
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "onConnected()");
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, REQUEST, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "onConnectionSuspended()");
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "onLocationChanged()");
        // Update the circle around the user location
        if (mUserCircle != null) {
            mUserCircle.setCenter(new LatLng(location.getLatitude(), location.getLongitude()));
        }

        // Center the map
        if (mNeedsCentering) {
            this.focusMyLocation(location);
            mNeedsCentering = false;
        }

        // If there is an auth token, get undiscovered Capsules in the new location
        if (this.mRetainedFragment != null) {
            // Make sure there is an auth token
            if (this.mAuthToken != null) {
                // Make sure there is a connection to the Google API
                if (this.mGoogleApiClient.isConnected()) {
                    this.mRetainedFragment.startCapsulePing(this, this.mAuthToken, location);
                } else {
                    Toast.makeText(this, this.getString(R.string.error_google_api_cannot_connect), Toast.LENGTH_SHORT).show();
                }
            } else {
                // There is no auth token, so get it on the background thread
                if (this.mAccount != null) {
                    this.mRetainedFragment.startAuthTokenRetrieval(this, this.mAccount);
                }
            }
        }
    }

    /**
     * Handles AuthTokenRetrievalTask doInBackground()
     *
     * @param params Account to retrieve an authentication token for
     * @return String The auth token
     */
    @Override
    public String duringAuthTokenRetrieval(Account... params) {
        Log.i(TAG, "duringAuthTokenRetrieval()");
        if (this.mAccountManager != null & this.mAccount != null) {
            try {
                return this.mAccountManager.blockingGetAuthToken(this.mAccount, Constants.AUTH_TOKEN_TYPE, true);
            } catch (OperationCanceledException | IOException | AuthenticatorException e) {
                // Cancel the auth token background task
                if (this.mRetainedFragment != null) {
                    this.mRetainedFragment.cancelAuthTokenRetrieval();
                }
                return null;
            }
        }
        return null;
    }

    /**
     * Handles AuthTokenRetrievalTask onPreExecute()
     */
    @Override
    public void onPreAuthTokenRetrieval() {
        Log.i(TAG, "onPreAuthTokenRetrieval()");
        if (this.mRetainedFragment != null) {
            this.mRetainedFragment.showProgress();
        }
    }

    /**
     * Handles AuthTokenRetrievalTask onPostExecute()
     *
     * @param authToken The retrieved authentication token
     */
    @Override
    public void onPostAuthTokenRetrieval(String authToken) {
        Log.i(TAG, "onPostAuthTokenRetrieval()");
        this.mAuthToken = authToken;
        if (this.mRetainedFragment != null) {
            this.mRetainedFragment.hideProgress();
        }
    }

    /**
     * Handles AuthTokenRetrievalTask onCancelled()
     */
    @Override
    public void onAuthTokenRetrievalCancelled() {
        Log.i(TAG, "onAuthTokenRetrievalCancelled()");
        if (this.mRetainedFragment != null) {
            this.mRetainedFragment.hideProgress();
        }
    }

    /**
     * Handles CapsulePingTask doInBackground()
     *
     * Sends HTTP request to API to get undiscovered Capsules
     *
     * @param params Authentication token, latitude, longitude
     * @return HTTP response object
     */
    @Override
    public CapsulePingResponse duringCapsulePing(String... params) {
        try {
            final String authToken = params[0];
            final String lat = params[1];
            final String lng = params[2];
            HttpResponse response = this.mRequestHandler.requestUndiscoveredCapsules(authToken, lat, lng);
            return new CapsulePingResponse(response);
        } catch (IOException | JSONException e) {
            // Cancel the task
            if (this.mRetainedFragment != null) {
                this.mRetainedFragment.cancelCapsulePing();
            }
            return null;
        }
    }

    /**
     * Handles CapsulePingTask onPreExecute()
     */
    @Override
    public void onPreCapsulePing() {
        Log.i(TAG, "onPreCapsulePing()");
    }

    /**
     * Handles CapsulePingTask onPostExecute()
     *
     * @param response HTTP response object for a Capsule ping
     */
    @Override
    public void onPostCapsulePing(CapsulePingResponse response) {
        if (response != null) {
            if (response.isClientError() || response.isServerError()) {
                // TODO If unauthenticated, prompt for login
                Toast.makeText(this, this.getString(R.string.error_cannot_retrieve_undiscovered), Toast.LENGTH_SHORT).show();
            } else {
                this.populateUndiscoveredMarkers(response.getCapsules());
            }
        }
    }

    /**
     * Handles CapsulePingTask onCancelled()
     */
    @Override
    public void onCapsulePingCancelled() {
        Log.i(TAG, "onCapsulePingCancelled()");
    }

    /**
     * Handles clicks on the navigation drawer
     *
     * @param drawerFragment The NavigationDrawerFragment
     * @param position The position of the item that was clicked
     * @param item The item that was clicked
     */
    @Override
    public void onNavigationDrawerItemClick(NavigationDrawerFragment drawerFragment, int position, NavigationDrawerItem item) {
        // Check if the selected item is in a group
        if (item.isInGroup()) {
            // See if the item is checked
            if (drawerFragment.getListView().isItemChecked(position)) {
                // TODO Perform the on-check action
            } else {
                // TODO Perform the un-check action
            }
        } else {
            // Prevent items not in a group from remaining checked
            drawerFragment.getListView().setItemChecked(position, false);
            // TODO Perform the action
        }

        // Close the drawer
        if (this.mDrawerLayout != null && this.mDrawerView != null) {
            this.mDrawerLayout.closeDrawer(this.mDrawerView);
        }
    }

    /**
     * Handles choosing an Account in the account switcher
     *
     * @param dialog The DialogFragment containing the switcher
     * @param account The Account that was at the selected position
     */
    @Override
    public void onAccountItemClick(AccountDialogFragment dialog, Account account) {
        // Switch the Account
        this.switchAccount(account);
        // Close the dialog
        dialog.dismiss();
    }

    /**
     * Gets a reference (if necessary) to the LocationClient.
     */
    private void setLocationClient() {
        // Only get the client if it is not already set
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
    }

    /**
     * Focuses the GoogleMap on user's location.
     * 
     * @param Location location
     */
    private void focusMyLocation(Location location) {
        if (location != null && mMap != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), ZOOM));
        }
    }

    /**
     * Handles the process of opening a Capsule marker.
     * 
     * @param capsule
     * @param owned
     */
    private void openCapsuleMarker(Capsule capsule, boolean owned) {
        Toast.makeText(getApplicationContext(), "Opened!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getApplicationContext(), CapsuleActivity.class);
        intent.putExtra("owned", owned);
        intent.putExtra("capsule", capsule);
        intent.putExtra("account_name", mAccount.name);
        if (owned) {
            startActivityForResult(intent, REQUEST_CODE_CAPSULE);
        } else {
            startActivity(intent);
        }
    }

    /**
     * Populates the Map with stored Capsule Markers.
     */
    private void populateStoredMarkers() {
        // Remove any previous Markers
        this.removeStoredMarkers();
        // (Re)initialize the collections
        mDiscoveredMarkers = new HashMap<Marker, Capsule>();
        mOwnedMarkers = new HashMap<Marker, Capsule>();

        // Populate the Discovery Markers
        Cursor c = getApplicationContext().getContentResolver().query(
                CapsuleContract.Discoveries.CONTENT_URI.buildUpon()
                .appendQueryParameter(CapsuleContract.QUERY_PARAM_JOIN, CapsuleContract.Capsules.TABLE_NAME)
                .build(),
                null,
                CapsuleContract.Discoveries.ACCOUNT_NAME + " = ?",
                new String[]{mAccount.name},
                null
        );
        while (c.moveToNext()) {
            Capsule capsule = new CapsulePojo(c);
            // Add the new Discovery Marker
            Marker marker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(capsule.getLatitude(), capsule.getLongitude()))
                .title(capsule.getName())
                .draggable(false)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            );
            // Maintain a mapping of the Capsule to the Marker
            mDiscoveredMarkers.put(marker, capsule);
        }
        c.close();

        // Populate the Ownership Markers
        c = getApplicationContext().getContentResolver().query(
                CapsuleContract.Ownerships.CONTENT_URI.buildUpon()
                .appendQueryParameter(CapsuleContract.QUERY_PARAM_JOIN, CapsuleContract.Capsules.TABLE_NAME)
                .build(),
                null,
                CapsuleContract.Ownerships.ACCOUNT_NAME + " = ?",
                new String[]{mAccount.name},
                null
        );
        while (c.moveToNext()) {
            Capsule capsule = new CapsulePojo(c);
            // Add the new Owned Marker
            Marker marker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(capsule.getLatitude(), capsule.getLongitude()))
                .title(capsule.getName())
                .draggable(false)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
            );
            // Maintain a mapping of the Capsule to the Marker
            mOwnedMarkers.put(marker, capsule);
        }
        c.close();
    }

    /**
     * Adds Undiscovered Markers to the map given a collection
     * 
     * Will keep track of Markers using a data structure so duplicates are not added.
     * 
     * TODO This function needs to be rewritten so that it:
     *  - Removes Markers that are no longer in the VISIBLE radius
     *  - Adds new Markers that have come into the VISIBLE radius
     *  - Leaves Markers already in the VISIBLE radius if they remain in it, but also updates the data
     * 
     * @param List<Capsule> capsules
     */
    private void populateUndiscoveredMarkers(List<Capsule> capsules) {
        // Remove all the old Markers
        this.removeUndiscoveredMarkers();
        // (Re)initialize the collection
        mUndiscoveredMarkers = new HashMap<Marker, Capsule>();

        // Add updated Markers from the server response
        for (int i = 0; i < capsules.size(); i++) {
            // The current capsule
            Capsule capsule = capsules.get(i);
            // Create the marker
            Marker marker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(capsule.getLatitude(), capsule.getLongitude()))
                .title(capsule.getName())
                .draggable(false)
            );
            // Maintain a mapping of the Capsule to the Marker
            mUndiscoveredMarkers.put(marker, capsule);
        }
    }

    /**
     * Removes stored Capsule Markers from the Map
     */
    private void removeStoredMarkers() {
        if (this.mDiscoveredMarkers != null) {
            this.removeMarkers(this.mDiscoveredMarkers.keySet());
        }
        if (this.mOwnedMarkers != null) {
            this.removeMarkers(this.mOwnedMarkers.keySet());
        }
    }

    /**
     * Removes undiscovered Capsule Markers from the Map
     */
    private void removeUndiscoveredMarkers() {
        if (this.mUndiscoveredMarkers != null) {
            this.removeMarkers(this.mUndiscoveredMarkers.keySet());
        }
    }

    /**
     * Given a colleciton of Markers, removes them from the Map
     *
     * @param markers Collection of markers to remove
     */
    private void removeMarkers(Collection<Marker> markers) {
        if (markers != null && markers.size() > 0) {
            for (Marker marker : markers) {
                marker.remove();
            }
        }
    }

    /**
     * Switches the Account for the Activity
     *
     * @param account The Account to switch to
     */
    private void switchAccount(Account account) {
        // Clear out all the Markers
        this.removeStoredMarkers();
        this.removeUndiscoveredMarkers();
        // Set the new Account
        this.mAccount = account;
        // Clear out the auth token
        this.mAuthToken = null;
        // If an Account was retained and it differs from the Account being switched to, reset any Account-dependent members
        if (this.mRetainedFragment != null) {
            // Get the retained Account
            Account retainedAccount = this.mRetainedFragment.getAccount();
            // If the retained Account is different than the last used one, reset any Account-dependent members
            if (retainedAccount == null || !account.name.equals(retainedAccount.name)) {
                // Cancel any running tasks
                this.mRetainedFragment.cancelTasks();
            }
            // If authentication is not already happening, do it on the background thread
            if (!this.mRetainedFragment.isRetrievingAuthToken()) {
                this.mRetainedFragment.startAuthTokenRetrieval(this, account);
            }
        }
        // Update the drawer Fragment
        if (this.mDrawerFragment != null) {
            this.mDrawerFragment.switchAccount(account);
        }
    }

    /**
     * Handler for clicks on the info window
     */
    private class InfoWindowListener implements OnInfoWindowClickListener {

        @Override
        public void onInfoWindowClick(Marker marker) {
            // Check if this is the new Capsule Marker
            if (marker.equals(mNewCapsuleMarker)) {
                Intent intent = new Intent(getApplicationContext(), CapsuleEditorActivity.class);
                intent.putExtra("latitude", marker.getPosition().latitude);
                intent.putExtra("longitude", marker.getPosition().longitude);
                intent.putExtra("account_name", mAccount.name);
                startActivityForResult(intent, REQUEST_CODE_CAPSULE_EDITOR);
                return;
            }

            // Owned Marker
            if (mOwnedMarkers.containsKey(marker)) {
                MainActivity.this.openCapsuleMarker(mOwnedMarkers.get(marker), true /* owned */);
                return;
            }

            // Discovered Marker
            if (mDiscoveredMarkers.containsKey(marker)) {
                MainActivity.this.openCapsuleMarker(mDiscoveredMarkers.get(marker), false /* not owned */);
                return;
            }

            // Undiscovered Marker
            if (mUndiscoveredMarkers.containsKey(marker)) {
                Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (location != null) {
                    double distance = SphericalUtil.computeDistanceBetween(
                            new LatLng(location.getLatitude(), location.getLongitude()),
                            marker.getPosition()
                    );
                    if (distance < DISCOVERY_RADIUS) {
                        new OpenCapsuleTask(MainActivity.this, mUndiscoveredMarkers.get(marker), marker).execute(mAuthToken);
                    } else {
                        Toast.makeText(getApplicationContext(), getText(R.string.map_outside_capsule_radius), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

    }

    /**
     * Handler for GoogleMap long clicks.
     * 
     * Currently long clicks allow for creating new Capsules.
     */
    private class MapLongClickListener implements OnMapLongClickListener {

        @Override
        public void onMapLongClick(final LatLng point) {
            // Build the Dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(getString(R.string.map_new_capsule_dialog_title)).setMessage(getString(R.string.map_new_capsule_dialog_message));

            // Confirm button
            builder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (mNewCapsuleMarker != null) {
                        mNewCapsuleMarker.remove();
                    }
                    mNewCapsuleMarker = mMap.addMarker(new MarkerOptions()
                        .position(point)
                        .title(getString(R.string.map_new_capsule_marker_infowindow_title))
                        .snippet(getString(R.string.map_new_capsule_marker_infowindow_snippet))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        .draggable(true)
                    );
                }

            });

            // Negative button
            builder.setNegativeButton(getString(android.R.string.cancel), null);

            // Create and show the Dialog
            AlertDialog dialog = builder.create();
            dialog.show();
        }

    }

    public class OpenCapsuleTask extends AsyncTask<String, Void, Uri> {

        /**
         * Shows a notification while the Task is being run in the background.
         */
        private ProgressDialog dialog;

        /**
         * The Capsule that is being opened.
         */
        private Capsule capsule;

        /**
         * The Marker corresponding to the Capsule.
         */
        private Marker marker;

        public OpenCapsuleTask(Activity activity, Capsule capsule, Marker marker) {
            this.dialog = new ProgressDialog(activity);
            this.capsule = capsule;
            this.marker = marker;
        }

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage(getText(R.string.map_during_open_capsule));
            this.dialog.show();
        }

        @Override
        protected Uri doInBackground(String... params) {
            Log.i(TAG, "OpenCapsuleTask.doInBackground()");
            // Attempt to get the last location
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            String response = null;
            if (location != null) {
                try {
                    response = mRequestHandler.requestOpenCapsule(params[0], this.capsule.getSyncId(), location.getLatitude(), location.getLongitude());
                } catch (NumberFormatException | ParseException | IOException e) {
                    e.printStackTrace();
                    this.cancel(true);
                }
                
            }
            // Parse the Discovery
            String etag = null;
            try {
                etag = JSONParser.parseOpenCapsule(response);
            } catch (JSONException e) {
                e.printStackTrace();
                this.cancel(true);
            }
            // INSERT the new Discovery
            Uri insertUri = null;
            if (etag != null) {
                insertUri = CapsuleOperations.insertDiscovery(getContentResolver(), this.capsule, mAccount.name, true /* setDirty */);
            } else {
                this.cancel(true);
            }

            return insertUri;
        }

        @Override
        protected void onCancelled(Uri result) {
            if (this.dialog.isShowing()) {
                this.dialog.hide();
                this.dialog.dismiss();
            }
        }

        @Override
        protected void onPostExecute(final Uri insertUri) {
            if (this.dialog.isShowing()) {
                this.dialog.hide();
                this.dialog.dismiss();
            }
            // Get the Capsule ID from the INSERT result URI
            long capsuleId = 0;
            if (insertUri.getQueryParameter(CapsuleContract.Discoveries.CAPSULE_ID) != null) {
                capsuleId = Long.valueOf(insertUri.getQueryParameter(CapsuleContract.Discoveries.CAPSULE_ID));
            }
            // Update the Capsule object and the Markers
            if (capsuleId > 0) {
                // Set the Capsule ID on the Capsule object
                this.capsule.setId(capsuleId);
                // Open the Capsule Marker
                MainActivity.this.openCapsuleMarker(this.capsule, false /* not owned */);
                // Remove the old, undiscovered Marker
                mUndiscoveredMarkers.remove(this.marker);
                this.marker.remove();
                // Add the new Discovery Marker
                Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(this.capsule.getLatitude(), this.capsule.getLongitude()))
                    .title(this.capsule.getName())
                    .draggable(false)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                );
                // Maintain a mapping of the Capsule sync id to the Marker
                mDiscoveredMarkers.put(marker, this.capsule);
            }
        }

    }

}
