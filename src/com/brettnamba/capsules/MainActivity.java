package com.brettnamba.capsules;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.json.JSONException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.brettnamba.capsules.dataaccess.Capsule;
import com.brettnamba.capsules.http.HttpFactory;
import com.brettnamba.capsules.http.RequestHandler;
import com.brettnamba.capsules.provider.CapsuleOperations;
import com.brettnamba.capsules.util.JSONParser;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;

/**
 * The main Activity that displays a GoogleMap and capsules to the user.
 * 
 * @author Brett
 *
 */
public class MainActivity extends ActionBarActivity
    implements
    ConnectionCallbacks,
    OnConnectionFailedListener,
    LocationListener {

    /**
     * Reference to the GoogleMap.
     */
    private GoogleMap mMap;

    /**
     * Reference to the Circle that surrounds the user's location, designating their "discovery" radius.
     */
    private Circle mUserCircle;

    /**
     * Reference to the LocationClient.
     */
    private LocationClient mLocationClient;

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
     * Maintains a reference to all the markers added to the map.
     */
    private Map<String, Long> mMarkers;

    /**
     * Maintains a Collection of all Capsules retrieved from the server.
     */
    private Map<Long, Capsule> mCapsules;

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
     * The tag used for logging.
     */
    private static final String TAG = "MainActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize
        this.getMap();
        this.getLocationClient();
        mAccountManager = AccountManager.get(this);
        mAccount = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE)[0];
        mHttpClient = HttpFactory.getInstance();
        mRequestHandler = new RequestHandler(mHttpClient);
        mMarkers = new HashMap<String, Long>();
        mCapsules = new HashMap<Long, Capsule>();
	}

	@Override
	protected void onResume() {
	    Log.i(TAG, "onResume()");
	    super.onResume();

	    // Re-initialize
	    this.getMap();
	    this.getLocationClient();
	    mLocationClient.connect();
	}

	@Override
	public void onPause() {
	    Log.i(TAG, "onPause()");
	    super.onPause();
        // Disconnect from the location client
	    if (mLocationClient != null) {
	        mLocationClient.disconnect();
	    }
	    // To re-center the map onResume, set the flag
	    mNeedsCentering = true;
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
		            // TODO Add discovered capsule markers
		        } else {
		            // TODO Remove discovered capsule markers
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
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "onConnectionFailed()");
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "onConnected()");
        mLocationClient.requestLocationUpdates(REQUEST, this);
    }

    @Override
    public void onDisconnected() {
        Log.i(TAG, "onDisconnected()");
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

        // Refresh the undiscovered capsules
        if (mAuthToken != null) {
            new CapsuleRequestTask().execute(mAuthToken);
        } else {
            new AuthTask(this).execute();
        }
    }

    /**
     * Gets a reference (if necessary) to the map Fragment.
     */
    private void getMap() {
        // Only get the map if it is not already set
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            // Specify map settings
            if (mMap != null) {
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
            }
        }
    }

    /**
     * Gets a reference (if necessary) to the LocationClient.
     */
    private void getLocationClient() {
        // Only get the client if it is not already set
        if (mLocationClient == null) {
            mLocationClient = new LocationClient(getApplicationContext(), this, this);
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
     * Adds Markers to the map
     * 
     * Will keep track of Markers using a data structure so duplicates are not added.
     * 
     * @param List<Capsule> capsules
     */
    private void addMarkers(List<Capsule> capsules) {
        for (int i = 0; i < capsules.size(); i++) {
            // Current capsule
            Capsule capsule = capsules.get(i);

            // Add the Capsule to the collection
            mCapsules.put(capsule.getSyncId(), capsule);

            // Add it if it is new
            if (!mMarkers.containsValue(capsule.getSyncId())) {
                Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(capsule.getLatitude(), capsule.getLongitude()))
                    .title(capsule.getName())
                    .draggable(false)
                );
                mMarkers.put(marker.getId(), capsule.getSyncId());
            }
        }
    }

    /**
     * Handler for clicks on the info window
     */
    private class InfoWindowListener implements OnInfoWindowClickListener {

        @Override
        public void onInfoWindowClick(Marker marker) {
            long syncId = mMarkers.get(marker.getId());
            if (!CapsuleOperations.isDiscovered(getContentResolver(), syncId, mAccount.name)) {
                Location location = mLocationClient.getLastLocation();
                if (location != null) {
                    double distance = SphericalUtil.computeDistanceBetween(
                            new LatLng(location.getLatitude(), location.getLongitude()),
                            marker.getPosition()
                    );
                    if (distance < DISCOVERY_RADIUS) {
                        // TODO Use a transaction
                        long insertId = CapsuleOperations.insertCapsule(getContentResolver(), mCapsules.get(syncId));
                        CapsuleOperations.insertDiscovery(getContentResolver(), insertId, mAccount.name);
                    } else {
                        Toast.makeText(getApplicationContext(), getText(R.string.map_outside_capsule_radius), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            if (CapsuleOperations.isDiscovered(getContentResolver(), syncId, mAccount.name)) {
                // TODO Open an Activity containing the Capsule data
            }
        }

    }

    /**
     * Background task for sending a HTTP request to the server to get undiscovered capsules.
     */
    public class CapsuleRequestTask extends AsyncTask<String, Void, List<Capsule>> {

        @Override
        protected List<Capsule> doInBackground(String... params) {
            Log.i(TAG, "CapsuleRequestTask.doInBackground()");
            // Attempt to get the last location
            Location location = mLocationClient.getLastLocation();
            if (location != null) {
                // Request the undiscovered Capsules
                String response = null;
                try {
                    response = mRequestHandler.requestUndiscoveredCapsules(params[0], location.getLatitude(), location.getLongitude());
                } catch (ParseException | IOException e) {
                    e.printStackTrace();
                    this.cancel(true);
                }

                // Parse the response
                List<Capsule> capsules = null;
                try {
                    capsules = JSONParser.parseUndiscoveredCapsules(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                    this.cancel(true);
                }
                return capsules;
            }
            return null;
        }

        @Override
        protected void onPostExecute(final List<Capsule> capsules) {
            Log.i(TAG, "CapsuleRequestTask.onPostExecute()");
            // Add the markers to the map
            MainActivity.this.addMarkers(capsules);
        }

    }

    /**
     * Background task for getting the current Account's authentication token.
     */
    public class AuthTask extends AsyncTask<Void, Void, String> {

        /**
         * Dialog to notify the user of the authentication process.
         */
        private ProgressDialog dialog;

        public AuthTask(Activity activity) {
            this.dialog = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage(getText(R.string.ui_activity_authenticating));
            this.dialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            Log.i(TAG, "AuthTask.doInBackground()");
            // Return the auth token
            try {
                return mAccountManager.blockingGetAuthToken(mAccount, Constants.AUTH_TOKEN_TYPE, true);
            } catch (OperationCanceledException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (AuthenticatorException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(final String authToken) {
            Log.i(TAG, "AuthTask.onPostExecute()");
            // Hide the dialog
            if (this.dialog.isShowing()) {
                this.dialog.hide();
                this.dialog.dismiss();
            }
            // Set the auth token on the corresponding activity property
            mAuthToken = authToken;
        }

    }
}
