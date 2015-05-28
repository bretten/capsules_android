package com.brettnamba.capsules.dataaccess;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.brettnamba.capsules.http.RequestContract;
import com.brettnamba.capsules.provider.CapsuleContract;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a Capsule
 *
 * @author Brett Namba
 */
public class Capsule implements Parcelable {

    /**
     * The app-specific primary key
     */
    protected long mId;

    /**
     * The server-specific unique identifier
     */
    protected long mSyncId;

    /**
     * The Capsule's name
     */
    protected String mName;

    /**
     * The Capsule's latitude
     */
    protected double mLatitude;

    /**
     * The Capsule's longitude
     */
    protected double mLongitude;


    /**
     * Constructor
     */
    public Capsule() {
    }

    /**
     * Constructor for rebuilding a copy of a Parcelable Capsule.
     *
     * @param in
     */
    protected Capsule(Parcel in) {
        this.mId = in.readLong();
        this.mSyncId = in.readLong();
        this.mName = in.readString();
        this.mLatitude = in.readDouble();
        this.mLongitude = in.readDouble();
    }

    /**
     * Constructs a Capsule using the data from the Cursor's current position.  This means
     * that the accessing the instance's getters does not pull from the database on the fly.
     *
     * @param c
     */
    public Capsule(Cursor c) {
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
    public Capsule(JSONObject jsonCapsule) throws JSONException {
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
    public long getId() {
        return this.mId;
    }

    /**
     * Gets the sync ID (the ID from the server)
     *
     * @return The sync ID of the Capsule
     */
    public long getSyncId() {
        return this.mSyncId;
    }

    /**
     * Gets the name of the Capsule
     *
     * @return The Capsule name
     */
    public String getName() {
        return this.mName;
    }

    /**
     * Gets the latitude of the Capsule
     *
     * @return The Capsule latitude
     */
    public double getLatitude() {
        return this.mLatitude;
    }

    /**
     * Gets the longitude of the Capsule
     *
     * @return The Capsule longitude
     */
    public double getLongitude() {
        return this.mLongitude;
    }

    /**
     * Sets the app-specific ID
     *
     * @param id The ID of the Capsule
     */
    public void setId(long id) {
        this.mId = id;
    }

    /**
     * Sets the server-specific ID
     *
     * @param syncId The sync ID of the Capsule
     */
    public void setSyncId(long syncId) {
        this.mSyncId = syncId;
    }

    /**
     * Sets the name
     *
     * @param name The name of the Capsule
     */
    public void setName(String name) {
        this.mName = name;
    }

    /**
     * Sets the latitude
     *
     * @param latitude The latitude of the Capsule
     */
    public void setLatitude(double latitude) {
        this.mLatitude = latitude;
    }

    /**
     * Sets the longitude
     *
     * @param longitude The longitude of the Capsule
     */
    public void setLongitude(double longitude) {
        this.mLongitude = longitude;
    }

    /**
     * Generates a hash code based on the ID and sync ID of the Capsule
     *
     * @return The integer hash code based on the ID and sync ID
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (this.mId ^ (this.mId >>> 32));
        result = prime * result + (int) (this.mSyncId ^ (this.mSyncId >>> 32));
        return result;
    }

    /**
     * Determines if this Capsule equals the specified object.  If it is not the same instance
     * it will then try to compare them based solely on the ID and the sync ID
     *
     * @param obj The object to be used in the comparison
     * @return True means they are equal, false means they are not
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Capsule other = (Capsule) obj;
        if (this.mId != other.mId) {
            return false;
        }
        if (this.mSyncId != other.mSyncId) {
            return false;
        }
        return true;
    }

    /**
     * Describes the contents for Parcelable
     *
     * @return The hash code of the Capsule
     */
    @Override
    public int describeContents() {
        return this.hashCode();
    }

    /**
     * Given a Parcel, writes this Capsule's data to it
     *
     * @param dest  The Parcel to write the Capsule data to
     * @param flags Flags for writing data
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // Write the class instance properties to the Parcel
        dest.writeLong(this.mId);
        dest.writeLong(this.mSyncId);
        dest.writeString(this.mName);
        dest.writeDouble(this.mLatitude);
        dest.writeDouble(this.mLongitude);
    }

    /**
     * Class that rebuilds a copy of the Capsule Parcelable.
     */
    public static final Parcelable.Creator<Capsule> CREATOR = new Parcelable.Creator<Capsule>() {

        /**
         * Instantiates a Capsule from a Parcel
         *
         * @param source Parcel to use in instantiation
         * @return The new Capsule
         */
        @Override
        public Capsule createFromParcel(Parcel source) {
            return new Capsule(source);
        }

        /**
         * Creates a Capsule array of the specified size
         *
         * @param size The size of the array to create
         * @return The new array
         */
        @Override
        public Capsule[] newArray(int size) {
            return new Capsule[size];
        }

    };

}
