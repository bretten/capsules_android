package com.brettnamba.capsules.syncadapter;

import android.app.Service;
import android.content.Intent;
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
                sSyncAdapter = new SyncAdapter(getApplicationContext(), true);
            }
        }
    }

}
