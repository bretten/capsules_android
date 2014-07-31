package com.brettnamba.capsules.dataaccess;

/**
 * Represents a Capsule in POJO form.  This is primarily used to build a Capsule object from
 * server response data.
 * 
 * @author Brett
 *
 */
public class CapsulePojo extends Capsule {

    /**
     * The application-specific primary key
     */
    private long mId;

    /**
     * The server-specific unique identifier
     */
    private long mSyncId;

    /**
     * The Capsule's name
     */
    private String mName;

    /**
     * The Capsule's latitude
     */
    private double mLatitude;

    /**
     * The Capsule's longitude
     */
    private double mLongitude;

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

    public CapsulePojo setId(long id) {
        this.mId = id;
        return this;
    }

    public CapsulePojo setSyncId(long syncId) {
        this.mSyncId = syncId;
        return this;
    }

    public CapsulePojo setName(String name) {
        this.mName = name;
        return this;
    }

    public CapsulePojo setLatitude(double latitude) {
        this.mLatitude = latitude;
        return this;
    }

    public CapsulePojo setLongitude(double longitude) {
        this.mLongitude = longitude;
        return this;
    }

}
