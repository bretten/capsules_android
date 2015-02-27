package com.brettnamba.capsules.dataaccess;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.brettnamba.capsules.provider.CapsuleContract;

/**
 * Represents a Capsule Cursor-backed POJO.
 * 
 * A cursor and a database's row's cursor position is required to instantiate this.  Whenever a getter
 * is called, the cursor moves to the row's position and retrieves the data on the fly.
 * 
 * If the columns were not specified in the query projection, the getters will return null or an equivalent.
 * 
 * @author Brett
 *
 */
public class CapsuleCojo extends Capsule {

    /**
     * The database cursor
     */
    private final Cursor mCursor;

    /**
     * The cursor position corresponding to this instance's database row
     */
    private final int mPosition;

    /**
     * Constructor
     * 
     * @param cursor
     * @param position
     */
    public CapsuleCojo(Cursor cursor, int position) {
        this.mCursor = cursor;
        this.mPosition = position;
    }

    /**
     * Constructor used by Parcelable to rebuild the data into another object.
     * 
     * NOTE: The cursor and cursor position are not retained since it is recreated from serialized data.
     * 
     * @param in
     */
    protected CapsuleCojo(Parcel in) {
        super(in);
        this.mCursor = null;
        this.mPosition = -1;
    }

    @Override
    public long getId() {
        this.mCursor.moveToPosition(this.mPosition);

        int i = this.mCursor.getColumnIndex(CapsuleContract.Capsules._ID);
        if (i == -1) {
            return 0;
        }

        return this.mCursor.getLong(i);
    }

    @Override
    public long getSyncId() {
        this.mCursor.moveToPosition(this.mPosition);

        int i = this.mCursor.getColumnIndex(CapsuleContract.Capsules.SYNC_ID);
        if (i == -1) {
            return 0;
        }

        return this.mCursor.getLong(i);
    }

    @Override
    public String getName() {
        this.mCursor.moveToPosition(this.mPosition);

        int i = this.mCursor.getColumnIndex(CapsuleContract.Capsules.NAME);
        if (i == -1) {
            return null;
        }

        return this.mCursor.getString(i);
    }

    @Override
    public double getLatitude() {
        this.mCursor.moveToPosition(this.mPosition);

        int i = this.mCursor.getColumnIndex(CapsuleContract.Capsules.LATITUDE);
        if (i == -1) {
            return 0;
        }

        return this.mCursor.getDouble(i);
    }

    @Override
    public double getLongitude() {
        this.mCursor.moveToPosition(this.mPosition);

        int i = this.mCursor.getColumnIndex(CapsuleContract.Capsules.LONGITUDE);
        if (i == -1) {
            return 0;
        }

        return this.mCursor.getDouble(i);
    }

    /**
     * Class that rebuilds a copy of the original Capsule object.
     */
    public static final Parcelable.Creator<Capsule> CREATOR = new Parcelable.Creator<Capsule>() {

        @Override
        public CapsuleCojo createFromParcel(Parcel source) {
            return new CapsuleCojo(source);
        }

        @Override
        public Capsule[] newArray(int size) {
            return new Capsule[size];
        }

    };

}
