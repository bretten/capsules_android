package com.brettnamba.capsules.syncadapter;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

/**
 * Service for tying together the sync adapter framework and this application's SyncAdapter.
 * 
 * @author Brett Namba
 *
 */
public class SyncService extends Service {

    /**
     * Holds a reference to the SyncAdapter.
     */
    private static SyncAdapter sSyncAdapter = null;

    /**
     * Reference to a LocationManager.
     */
    private static LocationManager sLocationManager = null;

    /**
     * Reference to a LocationListener.
     */
    private static LocationListener sLocationListener = null;

    /**
     * For thread-safe lock
     */
    private static final Object sSyncAdapterLock = new Object();

    /**
     * Gets the SyncAdapter binder.
     * 
     * @param Intent intent
     * @return IBinder
     */
    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }

    /**
     * Creates an instance of the SyncAdapter if it does not exist.
     */
    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                Context context = getApplicationContext();

                if (sLocationManager == null) {
                    sLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                }

                if (sLocationListener == null) {
                    sLocationListener = new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            // TODO Auto-generated method stub
                        }

                        @Override
                        public void onProviderDisabled(String provider) {
                            // TODO Auto-generated method stub
                        }

                        @Override
                        public void onProviderEnabled(String provider) {
                            // TODO Auto-generated method stub
                        }

                        @Override
                        public void onStatusChanged(String provider, int status, Bundle extras) {
                            // TODO Auto-generated method stub
                        }
                    };
                }

                // Register the LocationListener
                if (sLocationManager != null && sLocationListener != null) {
                    sLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, sLocationListener);
                }

                sSyncAdapter = new SyncAdapter(context, true, sLocationManager, sLocationListener);
            }
        }
    }

}
