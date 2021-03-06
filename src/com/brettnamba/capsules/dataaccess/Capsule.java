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
     * The total rating of the Capsule
     */
    protected int mTotalRating;

    /**
     * The number of times the Capsule was discovered
     */
    protected int mDiscoveryCount;

    /**
     * The number of times the Capsule was set as a favorite
     */
    protected int mFavoriteCount;

    /**
     * The owner of the Capsule
     */
    protected User mUser;

    /**
     * The Capsule's Memoir
     */
    protected Memoir mMemoir;

    /**
     * The Capsule's Discovery
     */
    protected Discovery mDiscovery;

    /**
     * The database cursor used when populating a Capsule instance on the fly from the database
     */
    protected Cursor mCursor;

    /**
     * The cursor position corresponding to this instance's database row
     */
    protected int mPosition = -1;

    /**
     * Constructor
     */
    public Capsule() {
    }

    /**
     * Constructor that clones the specified Capsule
     *
     * @param capsule The Capsule to clone
     */
    public Capsule(Capsule capsule) {
        this.mId = capsule.getId();
        this.mSyncId = capsule.getSyncId();
        this.mName = capsule.getName();
        this.mLatitude = capsule.getLatitude();
        this.mLongitude = capsule.getLongitude();
        this.mTotalRating = capsule.getTotalRating();
        this.mDiscoveryCount = capsule.getDiscoveryCount();
        this.mFavoriteCount = capsule.getFavoriteCount();
        this.mUser = capsule.getUser();
        this.mMemoir = capsule.getMemoir();
        this.mDiscovery = capsule.getDiscovery();
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
        this.mTotalRating = in.readInt();
        this.mDiscoveryCount = in.readInt();
        this.mFavoriteCount = in.readInt();
        this.mUser = in.readParcelable(User.class.getClassLoader());
        this.mMemoir = in.readParcelable(Memoir.class.getClassLoader());
        this.mDiscovery = in.readParcelable(Discovery.class.getClassLoader());
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
     * Special-case constructor that requires a database cursor and the row position of the Capsule.
     * Values will be pulled from the database on the fly as the getters are called and will be
     * retained on the Capsule's instance variables.  This minimizes data pulled from the database
     * and only loads it once it is needed.
     *
     * NOTE: If certain getters are not called, those values will not be loaded.  Cursors also need
     * to be manually closed after usage
     *
     * @param c
     * @param position
     */
    public Capsule(Cursor c, int position) {
        this.mCursor = c;
        this.mPosition = position;
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
        if (jsonCapsule.has(RequestContract.Field.CAPSULE_RATING)) {
            this.setTotalRating(jsonCapsule.getInt(RequestContract.Field.CAPSULE_RATING));
        }
        if (jsonCapsule.has(RequestContract.Field.CAPSULE_DISCOVERY_COUNT)) {
            this.setDiscoveryCount(
                    jsonCapsule.getInt(RequestContract.Field.CAPSULE_DISCOVERY_COUNT));
        }
        if (jsonCapsule.has(RequestContract.Field.CAPSULE_FAVORITE_COUNT)) {
            this.setFavoriteCount(jsonCapsule.getInt(RequestContract.Field.CAPSULE_FAVORITE_COUNT));
        }
    }

    /**
     * Gets the ID
     *
     * @return The ID of the Capsule
     */
    public long getId() {
        // Check if the value can be pulled from the database on the fly
        if (this.mCursor != null && this.mPosition >= 0 && this.mId == 0) {
            this.mCursor.moveToPosition(this.mPosition);

            int i = this.mCursor.getColumnIndex(CapsuleContract.Capsules._ID);
            if (i == -1) {
                return 0;
            }

            this.mId = this.mCursor.getLong(i);
        }

        return this.mId;
    }

    /**
     * Gets the sync ID (the ID from the server)
     *
     * @return The sync ID of the Capsule
     */
    public long getSyncId() {
        // Check if the value can be pulled from the database on the fly
        if (this.mCursor != null && this.mPosition >= 0 && this.mSyncId == 0) {
            this.mCursor.moveToPosition(this.mPosition);

            int i = this.mCursor.getColumnIndex(CapsuleContract.Capsules.SYNC_ID);
            if (i == -1) {
                return 0;
            }

            this.mSyncId = this.mCursor.getLong(i);
        }

        return this.mSyncId;
    }

    /**
     * Gets the name of the Capsule
     *
     * @return The Capsule name
     */
    public String getName() {
        // Check if the value can be pulled from the database on the fly
        if (this.mCursor != null && this.mPosition >= 0 && this.mName == null) {
            this.mCursor.moveToPosition(this.mPosition);

            int i = this.mCursor.getColumnIndex(CapsuleContract.Capsules.NAME);
            if (i == -1) {
                return null;
            }

            this.mName = this.mCursor.getString(i);
        }

        return this.mName;
    }

    /**
     * Gets the latitude of the Capsule
     *
     * @return The Capsule latitude
     */
    public double getLatitude() {
        // Check if the value can be pulled from the database on the fly
        if (this.mCursor != null && this.mPosition >= 0 && this.mLatitude == 0) {
            this.mCursor.moveToPosition(this.mPosition);

            int i = this.mCursor.getColumnIndex(CapsuleContract.Capsules.LATITUDE);
            if (i == -1) {
                return 0;
            }

            this.mLatitude = this.mCursor.getDouble(i);
        }

        return this.mLatitude;
    }

    /**
     * Gets the longitude of the Capsule
     *
     * @return The Capsule longitude
     */
    public double getLongitude() {
        // Check if the value can be pulled from the database on the fly
        if (this.mCursor != null && this.mPosition >= 0 && this.mLongitude == 0) {
            this.mCursor.moveToPosition(this.mPosition);

            int i = this.mCursor.getColumnIndex(CapsuleContract.Capsules.LONGITUDE);
            if (i == -1) {
                return 0;
            }

            this.mLongitude = this.mCursor.getDouble(i);
        }

        return this.mLongitude;
    }

    /**
     * Gets the total rating
     *
     * @return The total rating of the Capsule
     */
    public int getTotalRating() {
        return this.mTotalRating;
    }

    /**
     * Gets the discovery count
     *
     * @return The discovery count of the Capsule
     */
    public int getDiscoveryCount() {
        return this.mDiscoveryCount;
    }

    /**
     * Gets the favorite count
     *
     * @return The favorite count of the Capsule
     */
    public int getFavoriteCount() {
        return this.mFavoriteCount;
    }

    /**
     * Gets the owner of this Capsule
     *
     * @return The owner of this Capsule
     */
    public User getUser() {
        return this.mUser;
    }

    /**
     * Gets Capsule's Memoir
     *
     * @return The Capsule's Memoir
     */
    public Memoir getMemoir() {
        return this.mMemoir;
    }

    /**
     * Gets the Capsule's Discovery
     *
     * @return The Capsule's Discovery
     */
    public Discovery getDiscovery() {
        return this.mDiscovery;
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
     * Sets the total rating of the Capsule
     *
     * @param totalRating The total rating of the Capsule
     */
    public void setTotalRating(int totalRating) {
        this.mTotalRating = totalRating;
    }

    /**
     * Sets the discovery count
     *
     * @param discoveryCount The number of times the Capsule was discovered
     */
    public void setDiscoveryCount(int discoveryCount) {
        this.mDiscoveryCount = discoveryCount;
    }

    /**
     * Sets the favorite count
     *
     * @param favoriteCount The number of times the Capsule was set as a favorite
     */
    public void setFavoriteCount(int favoriteCount) {
        this.mFavoriteCount = favoriteCount;
    }

    /**
     * Sets the owner of this Capsule
     *
     * @param user The owner of this Capsule
     */
    public void setUser(User user) {
        this.mUser = user;
    }

    /**
     * Sets the Capsule's Memoir
     *
     * @param memoir The Memoir belonging to the Capsule
     */
    public void setMemoir(Memoir memoir) {
        this.mMemoir = memoir;
    }

    /**
     * Sets the Capsule's Discovery
     *
     * @param discovery The Discovery belonging to the Capsule
     */
    public void setDiscovery(Discovery discovery) {
        this.mDiscovery = discovery;
    }

    /**
     * Determines if the Capsule is a Discovery
     *
     * @return True if the Capsule is a Discovery
     */
    public boolean isDiscovery() {
        return this.mDiscovery != null;
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
        dest.writeInt(this.mTotalRating);
        dest.writeInt(this.mDiscoveryCount);
        dest.writeInt(this.mFavoriteCount);
        dest.writeParcelable(this.mUser, flags);
        dest.writeParcelable(this.mMemoir, flags);
        dest.writeParcelable(this.mDiscovery, flags);
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
