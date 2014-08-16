package com.brettnamba.capsules.dataaccess;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Abstraction of a Capsule class.
 * 
 * Subclasses will populate the object's data either from the context of the server (through HTTP response data)
 * or from a database cursor.
 * 
 * @author Brett Namba
 *
 */
public abstract class Capsule implements Parcelable {

    /**
     * The application-specific primary key
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
     * Returns the primary key.
     * 
     * @return long
     */
    abstract public long getId();

    /**
     * Returns the unique identifier from the server's context.
     * 
     * @return long
     */
    abstract public long getSyncId();

    /**
     * Returns the name of the Capsule.
     * 
     * @return String
     */
    abstract public String getName();

    /**
     * Returns the latitude.
     * 
     * @return double
     */
    abstract public double getLatitude();

    /**
     * Returns the longitude.
     * 
     * @return double
     */
    abstract public double getLongitude();

    /**
     * Constructor
     */
    public Capsule() {}

    /**
     * Constructor used by Parcelable.Creator to rebuild a Capsule object
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
     * Sets the ID.
     * 
     * @param id
     * @return
     */
    public Capsule setId(long id) {
        this.mId = id;
        return this;
    }

    /**
     * Sets the unique identifier from the server's context.
     * 
     * @param syncId
     * @return
     */
    public Capsule setSyncId(long syncId) {
        this.mSyncId = syncId;
        return this;
    }

    /**
     * Sets the name.
     * 
     * @param name
     * @return
     */
    public Capsule setName(String name) {
        this.mName = name;
        return this;
    }

    /**
     * Sets the latitude.
     * 
     * @param latitude
     * @return
     */
    public Capsule setLatitude(double latitude) {
        this.mLatitude = latitude;
        return this;
    }

    /**
     * Sets the longitude.
     * 
     * @param longitude
     * @return
     */
    public Capsule setLongitude(double longitude) {
        this.mLongitude = longitude;
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (mId ^ (mId >>> 32));
        result = prime * result + (int) (mSyncId ^ (mSyncId >>> 32));
        return result;
    }

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
        if (mId != other.mId) {
            return false;
        }
        if (mSyncId != other.mSyncId) {
            return false;
        }
        return true;
    }

    @Override
    public int describeContents() {
        return this.hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // Write the class instance properties to the Parcel
        dest.writeLong(this.mId);
        dest.writeLong(this.mSyncId);
        dest.writeString(this.mName);
        dest.writeDouble(this.mLatitude);
        dest.writeDouble(this.mLongitude);
    }

}
