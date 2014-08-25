package com.brettnamba.capsules.dataaccess;

import android.database.Cursor;

import com.brettnamba.capsules.provider.CapsuleContract;

/**
 * Represents an Ownership Capsule
 * 
 * @author Brett Namba
 *
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
    public CapsuleOwnershipPojo() {}

    /**
     * Constructor for instantiating a Ownership Capsule from a Cursor
     * 
     * @param c
     */
    public CapsuleOwnershipPojo(Cursor c) {
        super(c);

        int i = c.getColumnIndex(CapsuleContract.Ownerships._ID);
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
