package com.brettnamba.capsules.dataaccess;

/**
 * Represents a Capsule in POJO form.  This is primarily used to build a Capsule object from
 * server response data.
 * 
 * @author Brett
 *
 */
public class CapsulePojo extends Capsule {

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
