package com.brettnamba.capsules.provider;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.brettnamba.capsules.dataaccess.Capsule;
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
     * Creates a new Discovery row.
     * 
     * @param resolver
     * @param capsuleId
     * @param account
     * @return
     */
    public static long insertDiscovery(ContentResolver resolver, long capsuleId, String account) {
        ContentValues values = new ContentValues();
        values.put(CapsuleContract.Discoveries.CAPSULE_ID, String.valueOf(capsuleId));
        values.put(CapsuleContract.Discoveries.ACCOUNT_NAME, account);

        Uri uri = resolver.insert(CapsuleContract.Discoveries.CONTENT_URI, values);

        return ContentUris.parseId(uri);
    }

}