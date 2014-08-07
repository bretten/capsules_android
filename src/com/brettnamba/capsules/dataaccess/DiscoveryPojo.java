package com.brettnamba.capsules.dataaccess;

/**
 * Standard POJO implementation of a Discovery object.
 * 
 * @author Brett Namba
 *
 */
public class DiscoveryPojo extends Discovery {

    /**
     * The Discovery primary key
     */
    protected long mId;

    /**
     * The Discovery's Capsule foreign key
     */
    protected long mCapsuleId;

    /**
     * The Discovery's owner Account name
     */
    protected String mAccountName;

    /**
     * The Discovery's sync status
     */
    protected int mDirty;

    /**
     * The Discovery's favorite flag
     */
    protected int mFavorite;

    /**
     * The Discovery's rating
     */
    protected int mRating;

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

    public void setId(long id) {
        this.mId = id;
    }

    public void setCapsuleId(long capsuleId) {
        this.mCapsuleId = capsuleId;
    }

    public void setAccountName(String accountName) {
        this.mAccountName = accountName;
    }

    public void setDirty(int dirty) {
        this.mDirty = dirty;
    }

    public void setFavorite(int favorite) {
        this.mFavorite = favorite;
    }

    public void setRating(int rating) {
        this.mRating = rating;
    }

}
