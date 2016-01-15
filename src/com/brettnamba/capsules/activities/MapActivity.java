package com.brettnamba.capsules.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.brettnamba.capsules.Constants;
import com.brettnamba.capsules.R;
import com.brettnamba.capsules.authenticator.AccountDialogFragment;
import com.brettnamba.capsules.dataaccess.Capsule;
import com.brettnamba.capsules.fragments.NavigationDrawerFragment;
import com.brettnamba.capsules.fragments.RetainedMapFragment;
import com.brettnamba.capsules.http.RequestHandler;
import com.brettnamba.capsules.http.response.JsonResponse;
import com.brettnamba.capsules.os.AsyncListenerTask;
import com.brettnamba.capsules.util.Accounts;
import com.brettnamba.capsules.util.Widgets;
import com.brettnamba.capsules.widget.NavigationDrawerItem;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The main Activity that displays a GoogleMap and capsules to the user.
 *
 * @author Brett
 */
public class MapActivity extends FragmentActivity implements
        OnMapReadyCallback,
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        AccountDialogFragment.AccountDialogListener,
        NavigationDrawerFragment.NavigationDrawerListener,
        AsyncListenerTask.AuthTokenRetrievalTaskListener,
        AsyncListenerTask.DiscoverCapsulesTaskListener {

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
     * Collection of Discovery Capsules
     */
    private ArrayList<Capsule> mDiscoveryCapsules;

    /**
     * Reference to the GoogleApiClient.
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * Determines the accuracy of the location updates
     */
    private LocationRequest mLocationRequest;

    /**
     * Reference to the AccountManager.
     */
    private AccountManager mAccountManager;

    /**
     * Reference to the current Account.
     */
    private Account mAccount;

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
     * Slow interval for the LocationRequest (ms)
     */
    private static final int LOCATION_REQUEST_INTERVAL_SLOW = 10000;

    /**
     * Fast interval for the LocationRequest (ms)
     */
    private static final int LOCATION_REQUEST_INTERVAL_FAST = 5000;

    /**
     * The tag used for logging.
     */
    private static final String TAG = "MapActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_map);

        // Fragment manager
        FragmentManager fragmentManager = this.getSupportFragmentManager();

        // Get the Fragment that retains the background thread tasks
        this.mRetainedFragment = RetainedMapFragment.findOrCreate(fragmentManager);

        // Set up the GoogleMap
        SupportMapFragment mapFragment =
                ((SupportMapFragment) fragmentManager.findFragmentById(R.id.map));
        mapFragment.getMapAsync(this);

        // Build the Google Play services API client
        this.buildApiClient();

        // Build the LocationRequest
        this.buildLocationRequest();

        // AccountManager
        this.mAccountManager = AccountManager.get(this);

        // Check if there was any state data
        if (savedInstanceState != null) {
            // Recover the state data
            this.mDiscoveryCapsules =
                    savedInstanceState.getParcelableArrayList("discovery_capsules");
        } else {
            // Instantiate the Discovery Capsules collection
            this.mDiscoveryCapsules = new ArrayList<Capsule>();
        }

        // Navigation Drawer
        this.mDrawerFragment =
                (NavigationDrawerFragment) fragmentManager.findFragmentById(R.id.navigation_drawer);
        this.mDrawerLayout = (DrawerLayout) this.findViewById(R.id.drawer_layout);
        this.mDrawerView = this.findViewById(R.id.navigation_drawer);

        // Setup the Toolbar
        Toolbar toolbar = Widgets.createToolbar(this, this.getString(R.string.app_name), true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the drawer
                MapActivity.this.mDrawerLayout.openDrawer(MapActivity.this.mDrawerView);
            }
        });
    }

    /**
     * onResume
     */
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
        if (this.mGoogleApiClient != null && !this.mGoogleApiClient.isConnected()) {
            this.mGoogleApiClient.connect();
        }
    }

    /**
     * onPause
     */
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

        // Disconnect from the Google Play services location API
        if (this.mGoogleApiClient != null && this.mGoogleApiClient.isConnected()) {
            // Remove location listener updates
            LocationServices.FusedLocationApi.removeLocationUpdates(this.mGoogleApiClient, this);
            // Disconnect
            this.mGoogleApiClient.disconnect();
        }
    }

    /**
     * onSaveInstanceState
     *
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the Capsule collection
        outState.putParcelableArrayList("discovery_capsules", this.mDiscoveryCapsules);
    }

    /**
     * onStop
     */
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

    /**
     * Called when the GoogleMap is ready
     *
     * @param map The GoogleMap
     */
    @Override
    public void onMapReady(GoogleMap map) {
        Log.i(TAG, "onMapReady()");
        this.mMap = map;

        // Specify map settings
        this.mMap.setMyLocationEnabled(true);
        // Create the circle
        this.mUserCircle = this.mMap.addCircle(new CircleOptions()
                        .center(new LatLng(0, 0))
                        .radius(DISCOVERY_RADIUS)
                        .strokeWidth(0)
                        .fillColor(USER_CIRCLE_COLOR)
        );
        // Add any stored Capsules
        if (this.mDiscoveryCapsules != null && this.mDiscoveryCapsules.size() > 0) {
            this.addCapsulesAsMarkers(this.mDiscoveryCapsules);
        }
    }

    /**
     * Called when a connection could not be established with the Google API service
     *
     * @param result A result indicating what caused the failure
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "onConnectionFailed()");
        // Notify the user the Google API services could not be reached
        Toast.makeText(this, this.getString(R.string.error_google_api_cannot_connect),
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Called after the Google API has been connected to
     *
     * @param connectionHint Data provided by the Google API services
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "onConnected()");
        // Request location updates
        LocationServices.FusedLocationApi.requestLocationUpdates(this.mGoogleApiClient,
                this.mLocationRequest, this);
        // If the user's last known location is known, move there
        Location location =
                LocationServices.FusedLocationApi.getLastLocation(this.mGoogleApiClient);
        if (location != null) {
            this.focusOnLocation(location);
        }
    }

    /**
     * Called when the Google API connection has been suspended
     *
     * @param i The cause for the disconnection
     */
    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "onConnectionSuspended()");
        // If a request for undiscovered Capsules is running on the background thread, stop it
        if (this.mRetainedFragment != null) {
            this.mRetainedFragment.cancelCapsulePing();
        }
    }

    /**
     * Called whenever the Location is updated
     *
     * @param location The updated Location
     */
    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "onLocationChanged()");
        // Update the circle around the user location
        if (this.mUserCircle != null && this.isUserCirclePositionDifferent(location)) {
            this.mUserCircle.setCenter(new LatLng(location.getLatitude(), location.getLongitude()));
        }
        // Discover any nearby Capsules
        if (this.mRetainedFragment != null) {
            this.mRetainedFragment.startDiscoverCapsules(this, this.mAccount, location);
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
                return this.mAccountManager
                        .blockingGetAuthToken(this.mAccount, Constants.AUTH_TOKEN_TYPE, true);
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
     * Should handle doInBackground() work
     *
     * @param params The latitude and longitude of the device's location
     * @return The Capsules that were discovered
     */
    @Override
    public List<Capsule> duringDiscoverCapsules(Double... params) {
        Log.i(TAG, "duringDiscoverCapsules()");
        Double lat = params[0];
        Double lng = params[1];
        if (!lat.isNaN() && !lng.isNaN()) {
            JsonResponse response = RequestHandler
                    .requestDiscoverCapsules(this.getApplicationContext(), this.mAccount, lat, lng);
            return response.getCapsules();
        }
        return null;
    }

    /**
     * Should handle onPreExecute() work
     */
    @Override
    public void onPreDiscoverCapsules() {
        Log.i(TAG, "onPreDiscoverCapsules()");
        // Set the LocationServices fastest interval to a slower value during the network request
        this.setLocationRequestFastestInterval(LOCATION_REQUEST_INTERVAL_SLOW);
    }

    /**
     * Should handle onPostExecute() work
     *
     * @param capsules The Capsules that were discovered
     */
    @Override
    public void onPostDiscoverCapsules(List<Capsule> capsules) {
        Log.i(TAG, "onPostDiscoverCapsules()");
        if (capsules != null && capsules.size() > 0) {
            // Add the Capsules to the collection
            this.mDiscoveryCapsules.addAll(capsules);
            // Add them as Markers
            this.addCapsulesAsMarkers(capsules);
        }
        // Set the LocationRequest fastest interval back to a quicker value now that the request is done
        this.setLocationRequestFastestInterval(LOCATION_REQUEST_INTERVAL_FAST);
    }

    /**
     * Should handle onCancelled() work
     */
    @Override
    public void onDiscoverCapsulesCancelled() {
        Log.i(TAG, "onDiscoverCapsulesCancelled()");
        // Set the LocationRequest fastest interval back to a faster value
        this.setLocationRequestFastestInterval(LOCATION_REQUEST_INTERVAL_FAST);
    }

    /**
     * Handles clicks on the navigation drawer
     *
     * @param drawerFragment The NavigationDrawerFragment
     * @param position       The position of the item that was clicked
     * @param item           The item that was clicked
     */
    @Override
    public void onNavigationDrawerItemClick(NavigationDrawerFragment drawerFragment, int position,
                                            NavigationDrawerItem item) {
        NavigationDrawerFragment.handleItemClick(this, this.mAccount, drawerFragment, position,
                item);

        // Close the drawer
        if (this.mDrawerLayout != null && this.mDrawerView != null) {
            this.mDrawerLayout.closeDrawer(this.mDrawerView);
        }
    }

    /**
     * Handles choosing an Account in the account switcher
     *
     * @param dialog  The DialogFragment containing the switcher
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
     * Builds the Google Play services API client
     */
    private void buildApiClient() {
        // Only get the client if it is not already set
        if (this.mGoogleApiClient == null) {
            this.mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
    }

    /**
     * Builds the LocationRequest that determines the accuracy and frequency of the
     * location updates
     */
    private void buildLocationRequest() {
        if (this.mLocationRequest == null) {
            this.mLocationRequest = LocationRequest.create()
                    .setInterval(LOCATION_REQUEST_INTERVAL_SLOW)
                    .setFastestInterval(LOCATION_REQUEST_INTERVAL_FAST)
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }
    }

    /**
     * Sets the fastest interval on the LocationRequest
     *
     * @param ms The new fastest interval in milliseconds
     */
    private void setLocationRequestFastestInterval(int ms) {
        if (this.mGoogleApiClient.isConnected()) {
            // Make sure the LocationRequest member is instantiated
            this.buildLocationRequest();
            // Set the fastest interval
            this.mLocationRequest.setFastestInterval(ms);
            // Request location updates with the new parameters
            LocationServices.FusedLocationApi.requestLocationUpdates(this.mGoogleApiClient,
                    this.mLocationRequest, this);
        }
    }

    /**
     * Moves the Map camera to the specified Location.
     *
     * @param location The location to move the camera to
     */
    private void focusOnLocation(Location location) {
        if (location != null && this.mMap != null) {
            this.mMap.moveCamera(CameraUpdateFactory
                    .newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()),
                            ZOOM));
        }
    }

    /**
     * Determines if the user Circle is in a centered on a different location than the one specified
     *
     * @param location The location to compare the Circle's center to
     * @return True if the location is different, otherwise false
     */
    private boolean isUserCirclePositionDifferent(Location location) {
        if (this.mUserCircle == null) {
            return false;
        }

        // Get the Circle's position
        LatLng circleLatLng = this.mUserCircle.getCenter();

        return circleLatLng.latitude != location.getLatitude() ||
                circleLatLng.longitude != location.getLongitude();
    }

    /**
     * Adds the specified Capsules as Markers to the GoogleMap
     *
     * @param capsules The collection of Capsules
     */
    private void addCapsulesAsMarkers(List<Capsule> capsules) {
        if (this.mMap == null || capsules == null || capsules.size() < 1) {
            return;
        }

        for (Capsule capsule : capsules) {
            this.mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(capsule.getLatitude(), capsule.getLongitude())));
        }
    }

    /**
     * Switches the Account for the Activity
     *
     * @param account The Account to switch to
     */
    private void switchAccount(Account account) {
        // Set the new Account
        this.mAccount = account;
        // Update the drawer Fragment
        if (this.mDrawerFragment != null) {
            this.mDrawerFragment.switchAccount(account);
        }
    }

}
