package com.brettnamba.capsules.dataaccess;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.brettnamba.capsules.http.RequestContract;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a Capsule's Memoir.
 *
 * @author Brett Namba
 */
public class Memoir implements Parcelable {

    /**
     * The app-specific primary key
     */
    private long mId;

    /**
     * The server-side unique identifier
     */
    private long mSyncId;

    /**
     * The Memoir title
     */
    private String mTitle;

    /**
     * The message attached to the Memoir
     */
    private String mMessage;

    /**
     * The FileProvider content URI for the associated file upload
     */
    private Uri mFileContentUri;

    /**
     * Constructor
     */
    public Memoir() {
    }

    /**
     * Constructs a Memoir given a JSON object
     *
     * @param jsonObject A JSON object representing a Memoir
     * @throws JSONException
     */
    public Memoir(JSONObject jsonObject) throws JSONException {
        if (jsonObject.has(RequestContract.Field.MEMOIR_SYNC_ID)) {
            this.setSyncId(jsonObject.getLong(RequestContract.Field.MEMOIR_SYNC_ID));
        }
        if (jsonObject.has(RequestContract.Field.MEMOIR_TITLE)) {
            this.setTitle(jsonObject.getString(RequestContract.Field.MEMOIR_TITLE));
        }
        if (jsonObject.has(RequestContract.Field.MEMOIR_MESSAGE)) {
            this.setMessage(jsonObject.getString(RequestContract.Field.MEMOIR_MESSAGE));
        }
    }

    /**
     * Constructor for rebuilding a copy of a Parcelable Memoir
     *
     * @param in The Parcel used to instantiate the Memoir
     */
    private Memoir(Parcel in) {
        this.mId = in.readLong();
        this.mSyncId = in.readLong();
        this.mTitle = in.readString();
        this.mMessage = in.readString();
        this.mFileContentUri = in.readParcelable(Uri.class.getClassLoader());
    }

    /**
     * Gets the app-specific ID
     *
     * @return The app-specific ID
     */
    public long getId() {
        return this.mId;
    }

    /**
     * Sets the app-specific ID
     *
     * @param id The app-specific ID
     */
    public void setId(long id) {
        this.mId = id;
    }

    /**
     * Gets the sync ID
     *
     * @return The sync ID
     */
    public long getSyncId() {
        return this.mSyncId;
    }

    /**
     * Sets the sync ID
     *
     * @param syncId The sync ID
     */
    public void setSyncId(long syncId) {
        this.mSyncId = syncId;
    }

    /**
     * Gets the Memoir title
     *
     * @return The Memoir title
     */
    public String getTitle() {
        return this.mTitle;
    }

    /**
     * Sets the Memoir title
     *
     * @param title The Memoir title
     */
    public void setTitle(String title) {
        this.mTitle = title;
    }

    /**
     * Gets the Memoir message
     *
     * @return The Memoir message
     */
    public String getMessage() {
        return this.mMessage;
    }

    /**
     * Sets the Memoir message
     *
     * @param message The Memoir message
     */
    public void setMessage(String message) {
        this.mMessage = message;
    }

    /**
     * Gets the FileProvider content URI for the associated file
     *
     * @return The FileProvider content URI for the associated file
     */
    public Uri getFileContentUri() {
        return this.mFileContentUri;
    }

    /**
     * Sets the FileProvider content URI for the associated file
     *
     * @param fileContentUri The FileProvider content URI for the associated file
     */
    public void setFileContentUri(Uri fileContentUri) {
        this.mFileContentUri = fileContentUri;
    }

    /**
     * Describes the contents for Parcelable
     *
     * @return The hash code of the Memoir
     */
    @Override
    public int describeContents() {
        return this.hashCode();
    }

    /**
     * Writes the Memoir's member variables to a destination Parcel with the specified flags
     *
     * @param dest  The destination Parcel
     * @param flags Flags for writing data
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mId);
        dest.writeLong(this.mSyncId);
        dest.writeString(this.mTitle);
        dest.writeString(this.mMessage);
        dest.writeParcelable(this.mFileContentUri, flags);
    }

    /**
     * Parcelable creator that instantiates a Memoir given a Parcel.
     */
    public static final Parcelable.Creator<Memoir> CREATOR = new Parcelable.Creator<Memoir>() {

        /**
         * Instantiates a Memoir from a Parcel
         *
         * @param source Parcel to use in instantiation
         * @return The instantiated Memoir
         */
        @Override
        public Memoir createFromParcel(Parcel source) {
            return new Memoir(source);
        }

        /**
         * Creates a Memoir array of the specified size
         *
         * @param size The size of the array to create
         * @return The new array
         */
        @Override
        public Memoir[] newArray(int size) {
            return new Memoir[size];
        }

    };

}
