package com.brettnamba.capsules.provider;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.brettnamba.capsules.dataaccess.Capsule;
import com.brettnamba.capsules.dataaccess.CapsulePojo;
import com.brettnamba.capsules.dataaccess.Discovery;
import com.brettnamba.capsules.dataaccess.DiscoveryPojo;
import com.brettnamba.capsules.provider.CapsuleContract.Capsules;
import com.brettnamba.capsules.provider.CapsuleContract.Discoveries;

/**
 * Used to handle common database operations.
 * 
 * @author Brett
 *
 */
public class CapsuleOperations {

    /**
     * Inserts a Capsule.
     * 
     * @param resolver
     * @param capsule
     * @return
     */
    public static long insertCapsule(ContentResolver resolver, Capsule capsule) {
        ContentValues values = new ContentValues();
        values.put(CapsuleContract.Capsules.SYNC_ID, capsule.getSyncId());
        values.put(CapsuleContract.Capsules.NAME, capsule.getName());
        values.put(CapsuleContract.Capsules.LATITUDE, capsule.getLatitude());
        values.put(CapsuleContract.Capsules.LONGITUDE, capsule.getLongitude());

        Uri uri = resolver.insert(CapsuleContract.Capsules.CONTENT_URI, values);

        return ContentUris.parseId(uri);
    }

    /**
     * Checks if the Capsule has been discovered by the specified user
     * given the unique id from the server.
     * 
     * @param resolver
     * @param syncId
     * @param account
     * @return
     */
    public static boolean isDiscovered(ContentResolver resolver, long syncId, String account) {
        Cursor c = resolver.query(
                Discoveries.CONTENT_URI,
                new String[]{"1"},
                Capsules.TABLE_NAME + "." + Capsules.SYNC_ID + " = ? AND " + Discoveries.TABLE_NAME + "." + Discoveries.ACCOUNT_NAME + " = ?",
                new String[]{String.valueOf(syncId), account},
                null
        );

        c.close();

        return (c.getCount() > 0) ? true : false;
    }

    /**
     * Creates a new Discovery row and also inserts the Capsule if it has not been added yet.
     * 
     * @param resolver
     * @param capsule
     * @param account
     * @return
     */
    public static long insertDiscovery(ContentResolver resolver, Capsule capsule, String account) {
        ContentValues values = new ContentValues();
        values.put(CapsuleContract.Capsules.SYNC_ID, capsule.getSyncId());
        values.put(CapsuleContract.Capsules.NAME, capsule.getName());
        values.put(CapsuleContract.Capsules.LATITUDE, capsule.getLatitude());
        values.put(CapsuleContract.Capsules.LONGITUDE, capsule.getLongitude());
        values.put(CapsuleContract.Discoveries.ACCOUNT_NAME, account);

        Uri uri = resolver.insert(CapsuleContract.Discoveries.CONTENT_URI, values);

        return ContentUris.parseId(uri);
    }

    /**
     * Creates a new Ownership row.
     * 
     * @param resolver
     * @param capsule
     * @param account
     * @return
     */
    public static long insertOwnership(ContentResolver resolver, Capsule capsule, String account) {
        ContentValues values = new ContentValues();
        values.put(CapsuleContract.Capsules.NAME, capsule.getName());
        values.put(CapsuleContract.Capsules.LATITUDE, capsule.getLatitude());
        values.put(CapsuleContract.Capsules.LONGITUDE, capsule.getLongitude());
        values.put(CapsuleContract.Ownerships.ACCOUNT_NAME, account);

        Uri uri = resolver.insert(CapsuleContract.Ownerships.CONTENT_URI, values);

        return ContentUris.parseId(uri);
    }

    /**
     * Gets an individual Capsule by the server sync id.
     * 
     * @param resolver
     * @param syncId
     * @return
     */
    public static Capsule getCapsule(ContentResolver resolver, long syncId) {
        Cursor c = resolver.query(
                Capsules.CONTENT_URI,
                new String[]{"*"},
                Capsules.TABLE_NAME + "." + Capsules.SYNC_ID + " = ?",
                new String[]{String.valueOf(syncId)},
                null
        );

        Capsule capsule = null;
        if (c.moveToFirst()) {
            capsule = new CapsulePojo();
            capsule.setId(c.getLong(c.getColumnIndex(Capsules._ID)));
            capsule.setSyncId(c.getLong(c.getColumnIndex(Capsules.SYNC_ID)));
            capsule.setName(c.getString(c.getColumnIndex(Capsules.NAME)));
            capsule.setLatitude(c.getDouble(c.getColumnIndex(Capsules.LATITUDE)));
            capsule.setLongitude(c.getDouble(c.getColumnIndex(Capsules.LONGITUDE)));
        }

        c.close();

        return capsule;
    }

    /**
     * Gets an individual Discovery given the Capsule sync id and the Account name.
     * 
     * @param resolver
     * @param syncId
     * @param account
     * @return
     */
    public static Discovery getDiscovery(ContentResolver resolver, long syncId, String account) {
        Cursor c = resolver.query(
                Discoveries.CONTENT_URI,
                new String[]{"*"},
                Capsules.TABLE_NAME + "." + Capsules.SYNC_ID + " = ? AND " + Discoveries.TABLE_NAME + "." + Discoveries.ACCOUNT_NAME + " = ?",
                new String[]{String.valueOf(syncId), account},
                null
        );

        Discovery discovery = null;
        if (c.moveToFirst()) {
            discovery = new DiscoveryPojo();
            discovery.setId(c.getLong(c.getColumnIndex(Discoveries._ID)));
            discovery.setCapsuleId(c.getLong(c.getColumnIndex(Discoveries.CAPSULE_ID)));
            discovery.setAccountName(c.getString(c.getColumnIndex(Discoveries.ACCOUNT_NAME)));
            discovery.setDirty(c.getInt(c.getColumnIndex(Discoveries.DIRTY)));
            discovery.setFavorite(c.getInt(c.getColumnIndex(Discoveries.FAVORITE)));
            discovery.setRating(c.getInt(c.getColumnIndex(Discoveries.RATING)));
        }

        c.close();

        return discovery;
    }

    /**
     * Updates an individual Discovery row given the Capsule sync id and the Account name.
     * 
     * @param resolver
     * @param values
     * @param syncId
     * @param account
     * @return
     */
    public static boolean updateDiscovery(ContentResolver resolver, ContentValues values, long syncId, String account) {
        // TODO Get rid of nested query
        int count = resolver.update(
                Discoveries.CONTENT_URI,
                values,
                Discoveries.TABLE_NAME + "." + Discoveries.CAPSULE_ID + " IN "
                + "(SELECT " + Capsules.TABLE_NAME + "." + Capsules._ID + " FROM " + Capsules.TABLE_NAME + " WHERE " + Capsules.TABLE_NAME + "." + Capsules.SYNC_ID + " = ?)"
                + " AND " + Discoveries.TABLE_NAME + "." + Discoveries.ACCOUNT_NAME + " = ?",
                new String[]{String.valueOf(syncId), account}
        );
        return count > 0;
    }

}
