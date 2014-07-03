package com.brettnamba.capsules;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.brettnamba.capsules.http.HttpFactory;
import com.brettnamba.capsules.http.RequestHandler;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * The main Activity that displays a GoogleMap and capsules to the user.
 * 
 * @author Brett
 *
 */
public class MainActivity extends FragmentActivity
    implements
    ConnectionCallbacks,
    OnConnectionFailedListener,
    LocationListener {

    /**
     * Reference to the GoogleMap.
     */
    private GoogleMap mMap;

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
    private Map<String, String> mMarkers;

    /**
     * The default zoom level.
     */
    private static final int ZOOM = 15;

    /**
     * Quality of Location service settings.
     */
    private static final LocationRequest REQUEST = LocationRequest.create()
            .setInterval(5000)
            .setFastestInterval(16)
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
        
        this.getMap();
        this.getLocationClient();
        mAccountManager = AccountManager.get(this);
        mAccount = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE)[0];
        mHttpClient = HttpFactory.getInstance();
        mRequestHandler = new RequestHandler(mHttpClient);
        mMarkers = new HashMap<String, String>();
	}

	@Override
	protected void onResume() {
	    Log.i(TAG, "onResume()");
	    super.onResume();
	    this.getMap();
	    this.getLocationClient();
	    mLocationClient.connect();
	}

	@Override
	public void onPause() {
	    Log.i(TAG, "onPause()");
	    super.onPause();
	    if (mLocationClient != null) {
	        mLocationClient.disconnect();
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
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
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
        this.focusMyLocation(location);
        if (mAuthToken != null) {
            new CapsuleRequestTask().execute(mAuthToken);
        } else {
            new AuthTask().execute();
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
     * @param JSONArray capsules
     */
    private void addMarkers(JSONArray capsules) {
        for (int i = 0; i < capsules.length(); i++) {
            JSONObject capsule = null;
            try {
                capsule = capsules.getJSONObject(i).getJSONObject("Capsule");
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // Add the marker if it is new
            try {
                if (!mMarkers.containsKey(capsule.getString("id"))) {
                    try {
                        Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(Double.parseDouble(capsule.getString("lat")), Double.parseDouble(capsule.getString("lng"))))
                            .title(capsule.getString("name"))
                            .snippet(capsule.getString("created"))
                            .draggable(true)
                        );
                        mMarkers.put(capsule.getString("id"), marker.getId());
                    } catch (NumberFormatException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * Background task for sending a HTTP request to the server to get undiscovered capsules.
     * 
     * @author Brett Namba
     *
     */
    public class CapsuleRequestTask extends AsyncTask<String, Void, JSONArray> {

        @Override
        protected JSONArray doInBackground(String... params) {
            Log.i(TAG, "CapsuleRequestTask.doInBackground()");
            // Attempt to get the last location
            Location location = mLocationClient.getLastLocation();
            if (location != null) {
                JSONArray capsules = null;
                try {
                    capsules = mRequestHandler.requestUndiscoveredCapsules(params[0], location.getLatitude(), location.getLongitude());
                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return capsules;
            }
            return null;
        }

        @Override
        protected void onPostExecute(final JSONArray capsules) {
            Log.i(TAG, "CapsuleRequestTask.onPostExecute()");
            MainActivity.this.addMarkers(capsules);
        }

    }

    /**
     * Background task for getting the current Account's authentication token.
     * 
     * @author Brett Namba
     *
     */
    public class AuthTask extends AsyncTask<Void, Void, String> {

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
            mAuthToken = authToken;
        }

    }
}
