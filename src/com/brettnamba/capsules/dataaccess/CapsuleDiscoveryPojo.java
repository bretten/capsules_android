package com.brettnamba.capsules.dataaccess;

import android.database.Cursor;

import com.brettnamba.capsules.http.RequestContract;
import com.brettnamba.capsules.provider.CapsuleContract;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a Discovery Capsule
 *
 * @author Brett Namba
 */
public class CapsuleDiscoveryPojo extends CapsulePojo {

    /**
     * The Discovery primary key
     */
    protected long mDiscoveryId;

    /**
     * The etag
     */
    protected String mEtag;

    /**
     * The owner's Account name
     */
    protected String mAccountName;

    /**
     * The server sync status flag
     */
    protected int mDirty;

    /**
     * The favorite flag
     */
    protected int mFavorite;

    /**
     * The rating
     */
    protected int mRating;

    /**
     * Constructor
     */
    public CapsuleDiscoveryPojo() {
    }

    /**
     * Constructs a CapsuleDiscoveryPojo given another instance of Capsule.  It will only get
     * common properties from the Capsule instance that was passed in
     *
     * @param capsule An instance of a Capsule
     */
    public CapsuleDiscoveryPojo(Capsule capsule) {
        this.setId(capsule.getId());
        this.setSyncId(capsule.getSyncId());
        if (capsule.getName() != null) {
            this.setName(capsule.getName());
        }
        this.setLatitude(capsule.getLatitude());
        this.setLongitude(capsule.getLongitude());
    }

    /**
     * Constructs a CapsuleDiscoveryPojo by pulling values from a Cursor
     *
     * @param c
     */
    public CapsuleDiscoveryPojo(Cursor c) {
        super(c);

        int i = c.getColumnIndex(CapsuleContract.Discoveries.DISCOVERY_ID_ALIAS);
        if (i != -1) {
            this.setDiscoveryId(c.getLong(i));
        }
        i = c.getColumnIndex(CapsuleContract.Discoveries.ETAG);
        if (i != -1) {
            this.setEtag(c.getString(i));
        }
        i = c.getColumnIndex(CapsuleContract.Discoveries.ACCOUNT_NAME);
        if (i != -1) {
            this.setAccountName(c.getString(i));
        }
        i = c.getColumnIndex(CapsuleContract.Discoveries.DIRTY);
        if (i != -1) {
            this.setDirty(c.getInt(i));
        }
        i = c.getColumnIndex(CapsuleContract.Discoveries.FAVORITE);
        if (i != -1) {
            this.setFavorite(c.getInt(i));
        }
        i = c.getColumnIndex(CapsuleContract.Discoveries.RATING);
        if (i != -1) {
            this.setRating(c.getInt(i));
        }
    }

    /**
     * Constructs a CapsuleDiscoveryPojo given a JSONObject
     *
     * @param jsonCapsule A JSONObject representing a Discovery
     * @throws JSONException
     */
    public CapsuleDiscoveryPojo(JSONObject jsonCapsule) throws JSONException {
        super(jsonCapsule);
        if (jsonCapsule.has(RequestContract.Field.DISCOVERY_ETAG)) {
            this.setEtag(jsonCapsule.getString(RequestContract.Field.DISCOVERY_ETAG));
        }
        if (jsonCapsule.has(RequestContract.Field.DISCOVERY_FAVORITE)) {
            this.setFavorite(jsonCapsule.getInt(RequestContract.Field.DISCOVERY_FAVORITE));
        }
        if (jsonCapsule.has(RequestContract.Field.DISCOVERY_RATING)) {
            this.setRating(jsonCapsule.getInt(RequestContract.Field.DISCOVERY_RATING));
        }
    }

    /**
     * Gets the Discovery primary key
     *
     * @return
     */
    public long getDiscoveryId() {
        return this.mDiscoveryId;
    }

    /**
     * Gets the etag
     *
     * @return
     */
    public String getEtag() {
        return this.mEtag;
    }

    /**
     * Gets the owner Account name
     *
     * @return
     */
    public String getAccountName() {
        return this.mAccountName;
    }

    /**
     * Gets the server sync status flag
     *
     * @return
     */
    public int getDirty() {
        return this.mDirty;
    }

    /**
     * Gets the favorite flag
     *
     * @return
     */
    public int getFavorite() {
        return this.mFavorite;
    }

    /**
     * Gets the rating
     *
     * @return
     */
    public int getRating() {
        return this.mRating;
    }

    /**
     * Sets the Discovery primary key
     *
     * @param discoveryId
     */
    public void setDiscoveryId(long discoveryId) {
        this.mDiscoveryId = discoveryId;
    }

    /**
     * Sets the etag
     *
     * @param etag
     */
    public void setEtag(String etag) {
        this.mEtag = etag;
    }

    /**
     * Sets the Account name
     *
     * @param accountName
     */
    public void setAccountName(String accountName) {
        this.mAccountName = accountName;
    }

    /**
     * Sets the server sync status flag
     *
     * @param dirty
     */
    public void setDirty(int dirty) {
        this.mDirty = dirty;
    }

    /**
     * Sets the favorite flag
     *
     * @param favorite
     */
    public void setFavorite(int favorite) {
        this.mFavorite = favorite;
    }

    /**
     * Sets the rating
     *
     * @param rating
     */
    public void setRating(int rating) {
        this.mRating = rating;
    }

}
