package com.brettnamba.capsules.dataaccess;

import android.database.Cursor;

import com.brettnamba.capsules.http.RequestContract;
import com.brettnamba.capsules.provider.CapsuleContract;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents an Ownership Capsule
 *
 * @author Brett Namba
 */
public class CapsuleOwnershipPojo extends CapsulePojo {

    /**
     * The Ownership id
     */
    protected long mOwnershipId;

    /**
     * The Ownership etag
     */
    protected String mEtag;

    /**
     * The Account name of the Capsule owner
     */
    protected String mAccountName;

    /**
     * Flag indicating if the Capsule needs to be synced
     */
    protected int mDirty;

    /**
     * Flag indicating if the Capsule was deleted locally
     */
    protected int mDeleted;

    /**
     * Constructor
     */
    public CapsuleOwnershipPojo() {
    }

    /**
     * Constructs a CapsuleOwnershipPojo given an instance of Capsule.  Will only take the
     * common properties from the Capsule instance
     *
     * @param capsule An instance of Capsule
     */
    public CapsuleOwnershipPojo(Capsule capsule) {
        this.setId(capsule.getId());
        this.setSyncId(capsule.getSyncId());
        if (capsule.getName() != null) {
            this.setName(capsule.getName());
        }
        this.setLatitude(capsule.getLatitude());
        this.setLongitude(capsule.getLongitude());
    }

    /**
     * Constructor for instantiating a Ownership Capsule from a Cursor
     *
     * @param c
     */
    public CapsuleOwnershipPojo(Cursor c) {
        super(c);

        int i = c.getColumnIndex(CapsuleContract.Ownerships.OWNERSHIP_ID_ALIAS);
        if (i != -1) {
            this.setOwnershipId(c.getLong(i));
        }
        i = c.getColumnIndex(CapsuleContract.Ownerships.ETAG);
        if (i != -1) {
            this.setEtag(c.getString(i));
        }
        i = c.getColumnIndex(CapsuleContract.Ownerships.ACCOUNT_NAME);
        if (i != -1) {
            this.setAccountName(c.getString(i));
        }
        i = c.getColumnIndex(CapsuleContract.Ownerships.DIRTY);
        if (i != -1) {
            this.setDirty(c.getInt(i));
        }
        i = c.getColumnIndex(CapsuleContract.Ownerships.DELETED);
        if (i != -1) {
            this.setDeleted(c.getInt(i));
        }
    }

    /**
     * Constructs a CapsuleOwnershipPojo given a JSONObject
     *
     * @param jsonCapsule A JSONObject representing an Ownership
     * @throws JSONException
     */
    public CapsuleOwnershipPojo(JSONObject jsonCapsule) throws JSONException {
        super(jsonCapsule);
        if (jsonCapsule.has(RequestContract.Field.CAPSULE_ETAG)) {
            this.setEtag(jsonCapsule.getString(RequestContract.Field.CAPSULE_ETAG));
        }
    }

    /**
     * Generates a hash code based only on the sync ID
     *
     * @return A hashcode representation
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (this.mSyncId ^ (this.mSyncId >>> 32));
        return result;
    }

    /**
     * Determines equality based on the object OR if the sync ID matches
     *
     * @param obj The object to compare
     * @return True if the objects match or if their sync ID's match, otherwise false
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
        CapsuleOwnershipPojo other = (CapsuleOwnershipPojo) obj;
        if (this.mSyncId != other.mSyncId) {
            return false;
        }
        return true;
    }

    /**
     * Returns the Ownership id
     *
     * @return
     */
    public long getOwnershipId() {
        return this.mOwnershipId;
    }

    /**
     * Returns the Ownership etag
     *
     * @return
     */
    public String getEtag() {
        return this.mEtag;
    }

    /**
     * Returns the Ownership Account name
     *
     * @return
     */
    public String getAccountName() {
        return this.mAccountName;
    }

    /**
     * Returns the sync flag
     *
     * @return
     */
    public int getDirty() {
        return this.mDirty;
    }

    /**
     * Returns the deleted flag
     *
     * @return
     */
    public int getDeleted() {
        return this.mDeleted;
    }

    /**
     * Sets the Ownership id
     *
     * @param ownershipId
     * @return
     */
    public Capsule setOwnershipId(long ownershipId) {
        this.mOwnershipId = ownershipId;
        return this;
    }

    /**
     * Sets the Ownership etag
     *
     * @param etag
     * @return
     */
    public Capsule setEtag(String etag) {
        this.mEtag = etag;
        return this;
    }

    /**
     * Sets the Account name
     *
     * @param accountName
     * @return
     */
    public Capsule setAccountName(String accountName) {
        this.mAccountName = accountName;
        return this;
    }

    /**
     * Sets the sync status
     *
     * @param dirty
     * @return
     */
    public Capsule setDirty(int dirty) {
        this.mDirty = dirty;
        return this;
    }

    /**
     * Sets the deleted flag
     *
     * @param deleted
     * @return
     */
    public Capsule setDeleted(int deleted) {
        this.mDeleted = deleted;
        return this;
    }

}
