package com.brettnamba.capsules.dataaccess;

/**
 * Abstraction of a Discovered Capsule.
 * 
 * @author Brett Namba
 *
 */
public abstract class Discovery {

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

    /**
     * Returns the Discovery row primary key
     * 
     * @return
     */
    abstract public long getId();

    /**
     * Returns the Discovery's Capsule foreign key
     * 
     * @return
     */
    abstract public long getCapsuleId();

    /**
     * Returns the Discovery's Account name
     * 
     * @return
     */
    abstract public String getAccountName();

    /**
     * Returns the Discovery row's sync status
     * 
     * @return
     */
    abstract public int getDirty();

    /**
     * Returns the Discovery row's favorite flag
     * 
     * @return
     */
    abstract public int getFavorite();

    /**
     * Returns the Discovery's rating
     * 
     * @return
     */
    abstract public int getRating();

    /**
     * Sets the ID
     * 
     * @param id
     * @return
     */
    public Discovery setId(long id) {
        this.mId = id;
        return this;
    }

    /**
     * Sets the Capsule ID foreign key
     * 
     * @param capsuleId
     * @return
     */
    public Discovery setCapsuleId(long capsuleId) {
        this.mCapsuleId = capsuleId;
        return this;
    }

    /**
     * Sets the Account name
     * 
     * @param accountName
     * @return
     */
    public Discovery setAccountName(String accountName) {
        this.mAccountName = accountName;
        return this;
    }

    /**
     * Sets the sync status
     * 
     * @param dirty
     * @return
     */
    public Discovery setDirty(int dirty) {
        this.mDirty = dirty;
        return this;
    }

    /**
     * Sets the favorite flag
     * 
     * @param favorite
     * @return
     */
    public Discovery setFavorite(int favorite) {
        this.mFavorite = favorite;
        return this;
    }

    /**
     * Sets the rating
     * 
     * @param rating
     * @return
     */
    public Discovery setRating(int rating) {
        this.mRating = rating;
        return this;
    }

}
