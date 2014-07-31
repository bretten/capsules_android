package com.brettnamba.capsules.dataaccess;

/**
 * Abstraction of a Capsule class.
 * 
 * Subclasses will populate the object's data either from the context of the server (through HTTP response data)
 * or from a database cursor.
 * 
 * @author Brett Namba
 *
 */
public abstract class Capsule {

    /**
     * Returns the primary key.
     * 
     * @return long
     */
    abstract public long getId();

    /**
     * Returns the unique identifier from the server's context
     * 
     * @return long
     */
    abstract public long getSyncId();

    /**
     * Returns the name of the Capsule.
     * 
     * @return String
     */
    abstract public String getName();

    /**
     * Returns the latitude.
     * 
     * @return double
     */
    abstract public double getLatitude();

    /**
     * Returns the longitude.
     * 
     * @return double
     */
    abstract public double getLongitude();

}
