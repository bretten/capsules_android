package com.brettnamba.capsules.dataaccess;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.brettnamba.capsules.http.RequestContract;
import com.brettnamba.capsules.provider.CapsuleContract;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a Discovery Capsule
 *
 * @author Brett Namba
 */
public class CapsuleDiscovery extends Capsule {

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
    public CapsuleDiscovery() {
    }

    /**
     * Constructs a CapsuleDiscovery given another instance of Capsule.  It will only get
     * common properties from the Capsule instance that was passed in
     *
     * @param capsule An instance of a Capsule
     */
    public CapsuleDiscovery(Capsule capsule) {
        this.setId(capsule.getId());
        this.setSyncId(capsule.getSyncId());
        if (capsule.getName() != null) {
            this.setName(capsule.getName());
        }
        this.setLatitude(capsule.getLatitude());
        this.setLongitude(capsule.getLongitude());
    }

    /**
     * Constructs a CapsuleDiscovery by pulling values from a Cursor
     *
     * @param c
     */
    public CapsuleDiscovery(Cursor c) {
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
     * Constructs a CapsuleDiscovery given a JSONObject
     *
     * @param jsonCapsule A JSONObject representing a Discovery
     * @throws JSONException
     */
    public CapsuleDiscovery(JSONObject jsonCapsule) throws JSONException {
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
     * Constructor that instantiates using a Parcel
     *
     * @param in Parcel to read data from
     */
    protected CapsuleDiscovery(Parcel in) {
        super(in);

        this.mDiscoveryId = in.readLong();
        this.mEtag = in.readString();
        this.mAccountName = in.readString();
        this.mDirty = in.readInt();
        this.mFavorite = in.readInt();
        this.mRating = in.readInt();
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

    /**
     * Writes the instance properties to the Parcel
     *
     * @param dest  Parcel to write data to
     * @param flags Flags for writing data
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeLong(this.mDiscoveryId);
        dest.writeString(this.mEtag);
        dest.writeString(this.mAccountName);
        dest.writeInt(this.mDirty);
        dest.writeInt(this.mFavorite);
        dest.writeInt(this.mRating);
    }

    /**
     * Creator used to instantiate a CapsuleDiscovery from a Parcel
     */
    public static final Parcelable.Creator<CapsuleDiscovery> CREATOR = new Parcelable.Creator<CapsuleDiscovery>() {

        /**
         * Instantiates a CapsuleDiscovery from a Parcel
         *
         * @param source Parcel to use in instantiation
         * @return The new CapsuleDiscovery
         */
        @Override
        public CapsuleDiscovery createFromParcel(Parcel source) {
            return new CapsuleDiscovery(source);
        }

        /**
         * Creates a CapsuleDiscovery array of the specified size
         *
         * @param size The size of the array to create
         * @return The new array
         */
        @Override
        public CapsuleDiscovery[] newArray(int size) {
            return new CapsuleDiscovery[size];
        }

    };

}
