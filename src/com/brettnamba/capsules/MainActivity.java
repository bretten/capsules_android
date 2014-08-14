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
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.brettnamba.capsules.activities.CapsuleActivity;
import com.brettnamba.capsules.activities.CapsuleEditorActivity;
import com.brettnamba.capsules.activities.CapsuleListActivity;
import com.brettnamba.capsules.dataaccess.Capsule;
import com.brettnamba.capsules.dataaccess.CapsulePojo;
import com.brettnamba.capsules.http.HttpFactory;
import com.brettnamba.capsules.http.RequestHandler;
import com.brettnamba.capsules.provider.CapsuleContract;
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
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
        mUndiscoveredMarkers = new HashMap<Marker, Capsule>();
        mDiscoveredMarkers = new HashMap<Marker, Capsule>();
        mOwnedMarkers = new HashMap<Marker, Capsule>();

        // Populate
        this.populateStoredMarkers();
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
		} else if (id == R.id.action_discoveries_list) {
            Intent intent = new Intent(getApplicationContext(), CapsuleListActivity.class);
            intent.putExtra("account_name", mAccount.name);
            startActivity(intent);
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
                // Create the long click listener
                mMap.setOnMapLongClickListener(new MapLongClickListener());
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
     * Handles the process of opening a Capsule marker.
     * 
     * @param capsuleId
     * @param owned
     */
    private void openCapsuleMarker(long capsuleId, boolean owned) {
        Toast.makeText(getApplicationContext(), "Opened!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getApplicationContext(), CapsuleActivity.class);
        intent.putExtra("owned", owned);
        intent.putExtra("capsule_id", capsuleId);
        intent.putExtra("account_name", mAccount.name);
        startActivity(intent);
    }

    /**
     * Populates the Map with stored Capsule Markers.
     */
    private void populateStoredMarkers() {
        // Populate the Discovery Markers
        Cursor c = getApplicationContext().getContentResolver().query(CapsuleContract.Discoveries.CONTENT_URI.buildUpon()
                .appendQueryParameter(CapsuleContract.QUERY_PARAM_JOIN, CapsuleContract.Capsules.TABLE_NAME)
                .build(),
                null, null, null, null
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
            // Add the new Discovery Marker
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
     * Adds Markers to the map
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
    private void addUndiscoveredMarkers(List<Capsule> capsules) {
        // Remove all the old Markers
        for (Marker marker : mUndiscoveredMarkers.keySet()) {
            marker.remove();
        }
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
                startActivity(intent);
                return;
            }

            // Owned Marker
            if (mOwnedMarkers.containsKey(marker)) {
                MainActivity.this.openCapsuleMarker(mOwnedMarkers.get(marker).getId(), true /* owned */);
                return;
            }

            // Discovered Marker
            if (mDiscoveredMarkers.containsKey(marker)) {
                MainActivity.this.openCapsuleMarker(mDiscoveredMarkers.get(marker).getId(), false /* not owned */);
                return;
            }

            // Undiscovered Marker
            if (mUndiscoveredMarkers.containsKey(marker)) {
                Location location = mLocationClient.getLastLocation();
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
            MainActivity.this.addUndiscoveredMarkers(capsules);
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
            Location location = mLocationClient.getLastLocation();
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
            boolean success = false;
            try {
                success = JSONParser.parseOpenCapsule(response);
            } catch (JSONException e) {
                e.printStackTrace();
                this.cancel(true);
            }
            // INSERT the new Discovery
            Uri insertUri = null;
            if (success) {
                insertUri = CapsuleOperations.insertDiscovery(getContentResolver(), this.capsule, mAccount.name);
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
                MainActivity.this.openCapsuleMarker(capsuleId, false /* not owned */);
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
