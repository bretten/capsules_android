package com.brettnamba.capsules.provider;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.brettnamba.capsules.dataaccess.Capsule;
import com.brettnamba.capsules.dataaccess.CapsuleDiscoveryPojo;
import com.brettnamba.capsules.dataaccess.CapsuleOwnershipPojo;
import com.brettnamba.capsules.dataaccess.CapsulePojo;
import com.brettnamba.capsules.provider.CapsuleContract.Capsules;
import com.brettnamba.capsules.provider.CapsuleContract.Discoveries;
import com.brettnamba.capsules.provider.CapsuleContract.Ownerships;

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
                Discoveries.CONTENT_URI.buildUpon()
                .appendQueryParameter(CapsuleContract.QUERY_PARAM_JOIN, CapsuleContract.Capsules.TABLE_NAME)
                .build(),
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
     * @param setDirty
     * @return
     */
    public static Uri insertDiscovery(ContentResolver resolver, Capsule capsule, String account, boolean setDirty) {
        ContentValues values = new ContentValues();
        values.put(CapsuleContract.Capsules.SYNC_ID, capsule.getSyncId());
        values.put(CapsuleContract.Capsules.NAME, capsule.getName());
        values.put(CapsuleContract.Capsules.LATITUDE, capsule.getLatitude());
        values.put(CapsuleContract.Capsules.LONGITUDE, capsule.getLongitude());
        values.put(CapsuleContract.Discoveries.ACCOUNT_NAME, account);

        // Determine the Content URI
        Uri uri = CapsuleContract.Discoveries.CONTENT_URI.buildUpon()
                .appendQueryParameter(CapsuleContract.QUERY_PARAM_TRANSACTION, CapsuleContract.Capsules.TABLE_NAME)
                .build();
        // Set the dirty sync flag
        if (setDirty) {
            uri = uri.buildUpon()
                    .appendQueryParameter(CapsuleContract.QUERY_PARAM_SET_DIRTY, CapsuleContract.QUERY_VALUE_TRUE)
                    .build();
        }

        return resolver.insert(
                uri,
                values
        );
    }

    /**
     * Creates a new Ownership row.
     * 
     * @param resolver
     * @param capsule
     * @param account
     * @param setDirty
     * @return
     */
    public static Uri insertOwnership(ContentResolver resolver, Capsule capsule, String account, boolean setDirty) {
        ContentValues values = new ContentValues();
        values.put(CapsuleContract.Capsules.NAME, capsule.getName());
        values.put(CapsuleContract.Capsules.LATITUDE, capsule.getLatitude());
        values.put(CapsuleContract.Capsules.LONGITUDE, capsule.getLongitude());
        values.put(CapsuleContract.Ownerships.ACCOUNT_NAME, account);

        // The Content URI
        Uri uri = CapsuleContract.Ownerships.CONTENT_URI.buildUpon()
                .appendQueryParameter(CapsuleContract.QUERY_PARAM_TRANSACTION, CapsuleContract.Capsules.TABLE_NAME)
                .build();
        // Whether or not the dirty sync flag should be set
        if (setDirty) {
            uri = uri.buildUpon()
                    .appendQueryParameter(CapsuleContract.QUERY_PARAM_SET_DIRTY, CapsuleContract.QUERY_VALUE_TRUE)
                    .build();
        }

        return resolver.insert(uri, values);
    }

    /**
     * Given a Capsule, determines if it needs to INSERT or UPDATE both the Capsule and the Ownership
     * 
     * TODO This function will be rewritten
     *  - Use a transaction
     *  - Possibly move this to the CapsuleProvider
     * 
     * @param resolver
     * @param account
     * @param capsule
     * @param setDirty
     * @return
     */
    public static boolean insertUpdateOwnership(ContentResolver resolver, String account, Capsule capsule, boolean setDirty) {
        boolean success = true;

        Cursor c = resolver.query(
                Capsules.CONTENT_URI,
                new String[]{CapsuleContract.Capsules._ID},
                CapsuleContract.Capsules.SYNC_ID + " = ?",
                new String[]{String.valueOf(capsule.getSyncId())},
                null
        );
        long capsuleId;
        if (c.getCount() > 0 && c.moveToFirst()) {
            capsuleId = c.getLong(c.getColumnIndex(CapsuleContract.Capsules._ID));
            ContentValues values = new ContentValues();
            values.put(CapsuleContract.Capsules.NAME, capsule.getName());
            int count = resolver.update(
                    CapsuleContract.Capsules.CONTENT_URI,
                    values,
                    CapsuleContract.Capsules._ID + " = ?",
                    new String[]{String.valueOf(capsuleId)}
            );
            if (count < 1) {
                success = false;
            }
        } else {
            capsuleId = CapsuleOperations.insertCapsule(resolver, capsule);
        }
        c.close();
        if (capsuleId < 1) {
            success = false;
        }

        c = resolver.query(
                Ownerships.CONTENT_URI.buildUpon()
                .appendQueryParameter(CapsuleContract.QUERY_PARAM_JOIN, CapsuleContract.Capsules.TABLE_NAME)
                .build(),
                new String[]{CapsuleContract.Ownerships.TABLE_NAME + "." + CapsuleContract.Ownerships._ID},
                Capsules.TABLE_NAME + "." + Capsules.SYNC_ID + " = ? AND " + Ownerships.TABLE_NAME + "." + Ownerships.ACCOUNT_NAME + " = ?",
                new String[]{String.valueOf(capsule.getSyncId()), account},
                null
        );
        ContentValues values = new ContentValues();
        values.put(CapsuleContract.Ownerships.ACCOUNT_NAME, account);
        values.put(CapsuleContract.Ownerships.CAPSULE_ID, capsuleId);
        values.put(CapsuleContract.Ownerships.ETAG, ((CapsuleOwnershipPojo) capsule).getEtag());
        if (c.getCount() > 0 && c.moveToFirst()) {
            int count = resolver.update(
                    Ownerships.CONTENT_URI,
                    values,
                    Ownerships._ID + " = ?",
                    new String[]{String.valueOf(c.getLong(c.getColumnIndex(Ownerships._ID)))}
            );
            if (count < 1) {
                success = false;
            }
        } else {
            Uri uri = resolver.insert(Ownerships.CONTENT_URI, values);
            if (uri == null) {
                success = false;
            }
        }
        c.close();

        return success;
    }

    /**
     * Gets an individual Capsule by id.
     * 
     * @param resolver
     * @param capsuleId
     * @return
     */
    public static Capsule getCapsule(ContentResolver resolver, long capsuleId) {
        Cursor c = resolver.query(
                Capsules.CONTENT_URI,
                new String[]{"*"},
                Capsules.TABLE_NAME + "." + Capsules._ID + " = ?",
                new String[]{String.valueOf(capsuleId)},
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
     * Returns Ownership Capsules belonging to the specified Account name.
     * 
     * @param resolver
     * @param account
     * @param onlyDirty
     * @return
     */
    public static List<Capsule> getOwnerships(ContentResolver resolver, String account, boolean onlyDirty) {
        Cursor c = resolver.query(
                Ownerships.CONTENT_URI.buildUpon()
                .appendQueryParameter(CapsuleContract.QUERY_PARAM_JOIN, Capsules.TABLE_NAME)
                .build(),
                new String[]{"*"},
                Ownerships.ACCOUNT_NAME + " = ?" + (onlyDirty ? " AND " + Ownerships.DIRTY + " != 0" : ""),
                new String[]{account},
                null
        );

        List<Capsule> capsules = new ArrayList<Capsule>();
        while (c.moveToNext()) {
            capsules.add(new CapsuleOwnershipPojo(c));
        }
        c.close();

        return capsules;
    }

    /**
     * Gets an individual Discovery given the Capsule id and the Account name.
     * 
     * @param resolver
     * @param capsuleId
     * @param account
     * @return
     */
    public static Capsule getDiscovery(ContentResolver resolver, long capsuleId, String account) {
        Cursor c = resolver.query(
                Discoveries.CONTENT_URI.buildUpon()
                .appendQueryParameter(CapsuleContract.QUERY_PARAM_JOIN, CapsuleContract.Capsules.TABLE_NAME)
                .build(),
                CapsuleContract.Discoveries.CAPSULE_JOIN_PROJECTION,
                Capsules.TABLE_NAME + "." + Capsules._ID + " = ? AND " + Discoveries.TABLE_NAME + "." + Discoveries.ACCOUNT_NAME + " = ?",
                new String[]{String.valueOf(capsuleId), account},
                null
        );

        Capsule discovery = null;
        if (c.moveToFirst()) {
            discovery = new CapsuleDiscoveryPojo(c);
        }

        c.close();

        return discovery;
    }

    /**
     * Updates an individual Discovery row given the Discovery id.
     * 
     * @param resolver
     * @param values
     * @param discoveryId
     * @param setDirty
     * @return
     */
    public static boolean updateDiscovery(ContentResolver resolver, ContentValues values, long discoveryId, boolean setDirty) {
        // Determine the Content URI
        Uri uri = ContentUris.withAppendedId(CapsuleContract.Discoveries.CONTENT_URI, discoveryId);
        // Flag whether or not to set the Discovery as dirty
        if (setDirty) {
            uri = uri.buildUpon()
                    .appendQueryParameter(CapsuleContract.QUERY_PARAM_SET_DIRTY, CapsuleContract.QUERY_VALUE_TRUE)
                    .build();
        }

        int count = resolver.update(
                uri,
                values,
                CapsuleContract.Discoveries._ID + " = ?",
                new String[]{String.valueOf(discoveryId)}
        );
        return count > 0;
    }

    /**
     * UPDATEs a Capsule row.
     * 
     * @param resolver
     * @param capsule
     * @param setDirty
     * @return
     */
    public static boolean updateCapsule(ContentResolver resolver, Capsule capsule, boolean setDirty) {
        ContentValues values = new ContentValues();
        values.put(CapsuleContract.Capsules.NAME, capsule.getName());
        // The Content URI
        Uri uri = ContentUris.withAppendedId(CapsuleContract.Capsules.CONTENT_URI, capsule.getId());
        // Whether or not to set the dirty sync flag
        if (setDirty) {
            uri = uri.buildUpon()
                    .appendQueryParameter(CapsuleContract.QUERY_PARAM_SET_DIRTY, CapsuleContract.QUERY_VALUE_TRUE)
                    .build();
        }
        int count = resolver.update(
                uri,
                values,
                null,
                null
        );
        return count > 0;
    }

    /**
     * DELETEs a Capsule row.
     * 
     * @param resolver
     * @param capsuleId
     * @return
     */
    public static boolean deleteCapsule(ContentResolver resolver, long capsuleId) {
        return 0 < resolver.delete(
                CapsuleContract.Capsules.CONTENT_URI,
                CapsuleContract.Capsules._ID + " = ?",
                new String[]{String.valueOf(capsuleId)}
        );
    }

}
