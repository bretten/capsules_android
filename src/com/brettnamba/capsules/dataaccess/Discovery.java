package com.brettnamba.capsules.dataaccess;

/**
 * Abstraction of a Discovered Capsule.
 * 
 * @author Brett Namba
 *
 */
public abstract class Discovery {

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

}
