package com.brettnamba.capsules.dataaccess;

import android.database.Cursor;

import com.brettnamba.capsules.provider.CapsuleContract;

/**
 * Represents a Capsule in POJO form.  This is primarily used to build a Capsule object from
 * server response data.
 * 
 * @author Brett
 *
 */
public class CapsulePojo extends Capsule {

    /**
     * Constructor
     */
    public CapsulePojo() {}

    /**
     * Constructs a Capsule using the data from the Cursor's current position.  This means
     * that the accessing the instance's getters does not pull from the database on the fly.
     * 
     * @param c
     */
    public CapsulePojo(Cursor c) {
        int i = c.getColumnIndex(CapsuleContract.Capsules._ID);
        if (i != -1) {
            this.setId(c.getLong(i));
        }
        i = c.getColumnIndex(CapsuleContract.Capsules.SYNC_ID);
        if (i != -1) {
            this.setSyncId(c.getLong(i));
        }
        i = c.getColumnIndex(CapsuleContract.Capsules.NAME);
        if (i != -1) {
            this.setName(c.getString(i));
        }
        i = c.getColumnIndex(CapsuleContract.Capsules.LATITUDE);
        if (i != -1) {
            this.setLatitude(c.getDouble(i));
        }
        i = c.getColumnIndex(CapsuleContract.Capsules.LONGITUDE);
        if (i != -1) {
            this.setLongitude(c.getDouble(i));
        }
    }

    @Override
    public long getId() {
        return this.mId;
    }

    @Override
    public long getSyncId() {
        return this.mSyncId;
    }

    @Override
    public String getName() {
        return this.mName;
    }

    @Override
    public double getLatitude() {
        return this.mLatitude;
    }

    @Override
    public double getLongitude() {
        return this.mLongitude;
    }

}
