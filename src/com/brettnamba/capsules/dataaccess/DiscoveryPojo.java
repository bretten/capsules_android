package com.brettnamba.capsules.dataaccess;

/**
 * Standard POJO implementation of a Discovery object.
 * 
 * @author Brett Namba
 *
 */
public class DiscoveryPojo extends Discovery {

    @Override
    public long getId() {
        return this.mId;
    }

    @Override
    public long getCapsuleId() {
        return this.mCapsuleId;
    }

    @Override
    public String getAccountName() {
        return this.mAccountName;
    }

    @Override
    public int getDirty() {
        return this.mDirty;
    }

    @Override
    public int getFavorite() {
        return this.mFavorite;
    }

    @Override
    public int getRating() {
        return this.mRating;
    }

}
