package com.brettnamba.capsules.dataaccess;

import android.os.Parcel;
import android.os.Parcelable;

import com.brettnamba.capsules.http.RequestContract;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a discovered Capsule
 *
 * @author Brett Namba
 */
public class Discovery implements Parcelable {

    /**
     * The app-specific primary key
     */
    private long mId;

    /**
     * The server-side unique identifier
     */
    private long mSyncId;

    /**
     * Whether or not this Discovery has been opened
     */
    private boolean mIsOpened;

    /**
     * Whether or not this Discovery is set as a favorite
     */
    private boolean mIsFavorite;

    /**
     * The rating of this Discovery
     */
    private int mRating;

    /**
     * Constructs a Discovery given a JSONObject
     *
     * @param jsonObject A JSON object representing a Discovery
     * @throws JSONException
     */
    public Discovery(JSONObject jsonObject) throws JSONException {
        if (jsonObject.has(RequestContract.Field.DISCOVERY_SYNC_ID)) {
            this.setSyncId(jsonObject.getLong(RequestContract.Field.DISCOVERY_SYNC_ID));
        }
        if (jsonObject.has(RequestContract.Field.DISCOVERY_OPENED)) {
            this.setIsOpened(jsonObject.getBoolean(RequestContract.Field.DISCOVERY_OPENED));
        }
        if (jsonObject.has(RequestContract.Field.DISCOVERY_FAVORITE)) {
            this.setIsFavorite(jsonObject.getBoolean(RequestContract.Field.DISCOVERY_FAVORITE));
        }
        if (jsonObject.has(RequestContract.Field.DISCOVERY_RATING)) {
            this.setRating(jsonObject.getInt(RequestContract.Field.DISCOVERY_RATING));
        }
    }

    /**
     * Constructor for rebuilding a copy of a Parcelable Discovery
     *
     * @param in The Parcel used to instantiate the Discovery
     */
    private Discovery(Parcel in) {
        this.mId = in.readLong();
        this.mSyncId = in.readLong();
        this.mIsOpened = in.readByte() != 0;
        this.mIsFavorite = in.readByte() != 0;
        this.mRating = in.readInt();
    }

    /**
     * Gets the ID
     *
     * @return The Discovery ID
     */
    public long getId() {
        return this.mId;
    }

    /**
     * Gets the sync ID
     *
     * @return The Discovery sync ID
     */
    public long getSyncId() {
        return this.mSyncId;
    }

    /**
     * Determines if the Discovery has been opened
     *
     * @return True if the Discovery has been opened, otherwise false
     */
    public boolean isOpened() {
        return this.mIsOpened;
    }

    /**
     * Determines if the Capsule has been set as a favorite
     *
     * @return True if the Discovery has been set as a favorite, otherwise false
     */
    public boolean isFavorite() {
        return this.mIsFavorite;
    }

    /**
     * Gets the rating of the Discovery
     *
     * @return The rating of the Discovery
     */
    public int getRating() {
        return this.mRating;
    }

    /**
     * Determines if the Discovery is rated up
     *
     * @return True if the Discovery is rated up
     */
    public boolean isRatedUp() {
        return this.mRating >= 1;
    }

    /**
     * Determines if the Discovery is rated down
     *
     * @return True if the Discovery is rated down
     */
    public boolean isRatedDown() {
        return this.mRating <= -1;
    }

    /**
     * Sets the ID of the Discovery
     *
     * @param id The ID of the Discovery
     */
    public void setId(long id) {
        this.mId = id;
    }

    /**
     * Sets the sync ID of the Discovery
     *
     * @param syncId The sync ID of the Discovery
     */
    public void setSyncId(long syncId) {
        this.mSyncId = syncId;
    }

    /**
     * Sets if this Discovery has been opened
     *
     * @param isOpened True if the Discovery has been opened, false if it has not been opened
     */
    public void setIsOpened(boolean isOpened) {
        this.mIsOpened = isOpened;
    }

    /**
     * Sets if this Discovery is a favorite
     *
     * @param isFavorite True if it is a favorite, otherwise false
     */
    public void setIsFavorite(boolean isFavorite) {
        this.mIsFavorite = isFavorite;
    }

    /**
     * Sets the rating for this Discovery
     *
     * @param rating The Discovery rating
     */
    public void setRating(int rating) {
        this.mRating = rating;
    }

    /**
     * Describes the contents for Parcelable
     *
     * @return The hash code of the Discovery
     */
    @Override
    public int describeContents() {
        return this.hashCode();
    }

    /**
     * Writes the Discovery's member variables to a destination Parcel with the specified flags
     *
     * @param dest  The destination Parcel
     * @param flags Flags for writing data
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mId);
        dest.writeLong(this.mSyncId);
        dest.writeByte((byte) (this.mIsOpened ? 1 : 0));
        dest.writeByte((byte) (this.mIsFavorite ? 1 : 0));
        dest.writeInt(this.mRating);
    }

    /**
     * Parcelable creator that instantiates a Discovery given a Parcel.
     */
    public static final Parcelable.Creator<Discovery> CREATOR =
            new Parcelable.Creator<Discovery>() {

                /**
                 * Instantiates a Discovery from a Parcel
                 *
                 * @param source Parcel to use in instantiation
                 * @return The instantiated Discovery
                 */
                @Override
                public Discovery createFromParcel(Parcel source) {
                    return new Discovery(source);
                }

                /**
                 * Creates a Discovery array of the specified size
                 *
                 * @param size The size of the array to create
                 * @return The new array
                 */
                @Override
                public Discovery[] newArray(int size) {
                    return new Discovery[size];
                }

            };

}
