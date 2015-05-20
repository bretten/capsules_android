package com.brettnamba.capsules.dataaccess;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.brettnamba.capsules.http.RequestContract;
import com.brettnamba.capsules.provider.CapsuleContract;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a Capsule in POJO form.  This is primarily used to build a Capsule object from
 * server response data.
 *
 * @author Brett Namba
 */
public class CapsulePojo extends Capsule {

    /**
     * Constructor
     */
    public CapsulePojo() {
    }

    /**
     * Constructor for rebuilding a copy of a Parcelable Capsule.
     *
     * @param in
     */
    protected CapsulePojo(Parcel in) {
        super(in);
    }

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

    /**
     * Constructs a Capsule given a JSONObject
     *
     * @param jsonCapsule A JSONObject representing a Capsule
     * @throws JSONException
     */
    public CapsulePojo(JSONObject jsonCapsule) throws JSONException {
        if (jsonCapsule.has(RequestContract.Field.CAPSULE_SYNC_ID)) {
            this.setSyncId(jsonCapsule.getLong(RequestContract.Field.CAPSULE_SYNC_ID));
        }
        if (jsonCapsule.has(RequestContract.Field.CAPSULE_NAME)) {
            this.setName(jsonCapsule.getString(RequestContract.Field.CAPSULE_NAME));
        }
        if (jsonCapsule.has(RequestContract.Field.CAPSULE_LATITUDE)) {
            this.setLatitude(jsonCapsule.getDouble(RequestContract.Field.CAPSULE_LATITUDE));
        }
        if (jsonCapsule.has(RequestContract.Field.CAPSULE_LONGITUDE)) {
            this.setLongitude(jsonCapsule.getDouble(RequestContract.Field.CAPSULE_LONGITUDE));
        }
    }

    /**
     * Gets the ID
     *
     * @return The ID of the Capsule
     */
    @Override
    public long getId() {
        return this.mId;
    }

    /**
     * Gets the sync ID (the ID from the server)
     *
     * @return The sync ID of the Capsule
     */
    @Override
    public long getSyncId() {
        return this.mSyncId;
    }

    /**
     * Gets the name of the Capsule
     *
     * @return The Capsule name
     */
    @Override
    public String getName() {
        return this.mName;
    }

    /**
     * Gets the latitude of the Capsule
     *
     * @return The Capsule latitude
     */
    @Override
    public double getLatitude() {
        return this.mLatitude;
    }

    /**
     * Gets the longitude of the Capsule
     *
     * @return The Capsule longitude
     */
    @Override
    public double getLongitude() {
        return this.mLongitude;
    }

    /**
     * Class that rebuilds a copy of the Capsule Parcelable.
     */
    public static final Parcelable.Creator<Capsule> CREATOR = new Parcelable.Creator<Capsule>() {

        @Override
        public CapsulePojo createFromParcel(Parcel source) {
            return new CapsulePojo(source);
        }

        @Override
        public Capsule[] newArray(int size) {
            return new Capsule[size];
        }

    };

}
