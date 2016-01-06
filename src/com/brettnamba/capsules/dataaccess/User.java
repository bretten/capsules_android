package com.brettnamba.capsules.dataaccess;

import android.os.Parcel;
import android.os.Parcelable;

import com.brettnamba.capsules.http.RequestContract;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a User of the system.
 *
 * @author Brett Namba
 */
public class User implements Parcelable {

    /**
     * The User's username
     */
    private String mUsername;

    /**
     * Constructor
     */
    public User() {
    }

    /**
     * Constructor that instantiates a User given a JSON object
     *
     * @param jsonObject A JSON object representing a User
     * @throws JSONException
     */
    public User(JSONObject jsonObject) throws JSONException {
        if (jsonObject.has(RequestContract.Field.USER_USERNAME)) {
            this.setUsername(jsonObject.getString(RequestContract.Field.USER_USERNAME));
        }
    }

    /**
     * Constructor for rebuilding a copy of a Parcelable User
     *
     * @param in The Parcel used to instantiate the User
     */
    protected User(Parcel in) {
        mUsername = in.readString();
    }

    /**
     * Gets the User's username
     *
     * @return The User's username
     */
    public String getUsername() {
        return this.mUsername;
    }

    /**
     * Sets the User's username
     *
     * @param username The User's username
     */
    public void setUsername(String username) {
        this.mUsername = username;
    }

    /**
     * Describes the contents for Parcelable
     *
     * @return The hash code of the User
     */
    @Override
    public int describeContents() {
        return this.hashCode();
    }

    /**
     * Writes the User's member variables to a destination Parcel with the specified flags
     *
     * @param dest  The destination Parcel
     * @param flags Flags for writing data
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mUsername);
    }

    /**
     * Parcelable creator that instantiates a User
     */
    public static final Creator<User> CREATOR = new Creator<User>() {

        /**
         * Instantiates a User with the specified Parcel
         *
         * @param in Parcel to use in instantiation
         * @return The User
         */
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        /**
         * Creates a User array of the specified size
         *
         * @param size The size of the array to create
         * @return The new array
         */
        @Override
        public User[] newArray(int size) {
            return new User[size];
        }

    };

}
