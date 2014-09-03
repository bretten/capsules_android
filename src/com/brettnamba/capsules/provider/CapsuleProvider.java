package com.brettnamba.capsules.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class CapsuleProvider extends ContentProvider {

    private static final String DATABASE_NAME = "capsules.db";

    private static final int DATABASE_VERSION = 1;

    private static final UriMatcher sUriMatcher;

    private static final int CODE_CAPSULES = 10;
    private static final int CODE_CAPSULES_WILD = 11;
    private static final int CODE_DISCOVERIES = 20;
    private static final int CODE_DISCOVERIES_WILD = 21;
    private static final int CODE_OWNERSHIPS = 30;
    private static final int CODE_OWNERSHIPS_WILD = 31;

    private static final int PATH_ID_POS = 1;

    private SQLiteOpenHelper mDbHelper;

    private SQLiteDatabase mDb;

    private static final String TAG = "CapsuleProvider";

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(CapsuleContract.AUTHORITY, CapsuleContract.Capsules.CONTENT_URI_PATH, CODE_CAPSULES);
        sUriMatcher.addURI(CapsuleContract.AUTHORITY, CapsuleContract.Capsules.CONTENT_URI_PATH + "/#", CODE_CAPSULES_WILD);
        sUriMatcher.addURI(CapsuleContract.AUTHORITY, CapsuleContract.Discoveries.CONTENT_URI_PATH, CODE_DISCOVERIES);
        sUriMatcher.addURI(CapsuleContract.AUTHORITY, CapsuleContract.Discoveries.CONTENT_URI_PATH + "/#", CODE_DISCOVERIES_WILD);
        sUriMatcher.addURI(CapsuleContract.AUTHORITY, CapsuleContract.Ownerships.CONTENT_URI_PATH, CODE_OWNERSHIPS);
        sUriMatcher.addURI(CapsuleContract.AUTHORITY, CapsuleContract.Ownerships.CONTENT_URI_PATH + "/#", CODE_OWNERSHIPS_WILD);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        String table;
        if (selection == null) {
            selection = "";
        }

        switch (sUriMatcher.match(uri)) {

        case CODE_CAPSULES :
            table = CapsuleContract.Capsules.TABLE_NAME;
            break;

        case CODE_CAPSULES_WILD :
            table = CapsuleContract.Capsules.TABLE_NAME;
            selection += ((!TextUtils.isEmpty(selection)) ? " AND " : "") + CapsuleContract.Capsules._ID + " = " + uri.getPathSegments().get(PATH_ID_POS);
            break;

        case CODE_DISCOVERIES :
            table = CapsuleContract.Discoveries.TABLE_NAME;
            break;

        case CODE_DISCOVERIES_WILD :
            table = CapsuleContract.Discoveries.TABLE_NAME;
            selection += ((!TextUtils.isEmpty(selection)) ? " AND " : "") + CapsuleContract.Discoveries._ID + " = " + uri.getPathSegments().get(PATH_ID_POS);
            break;

        case CODE_OWNERSHIPS :
            table = CapsuleContract.Ownerships.TABLE_NAME;
            break;

        case CODE_OWNERSHIPS_WILD :
            table = CapsuleContract.Ownerships.TABLE_NAME;
            selection += ((!TextUtils.isEmpty(selection)) ? " AND " : "") + CapsuleContract.Ownerships._ID + " = " + uri.getPathSegments().get(PATH_ID_POS);
            break;

        default :
            throw new IllegalArgumentException("Unknown URI: " + uri);

        }

        mDb = mDbHelper.getWritableDatabase();

        int count = mDb.delete(table, selection, selectionArgs);

        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

    @Override
    public String getType(Uri uri) {
        String table;
        String subType;

        switch (sUriMatcher.match(uri)) {

        case CODE_CAPSULES :
            subType = "dir";
            table = CapsuleContract.Capsules.TABLE_NAME;
            break;

        case CODE_CAPSULES_WILD :
            subType = "item";
            table = CapsuleContract.Capsules.TABLE_NAME;
            break;

        case CODE_DISCOVERIES :
            subType = "dir";
            table = CapsuleContract.Discoveries.TABLE_NAME;
            break;

        case CODE_DISCOVERIES_WILD :
            subType = "item";
            table = CapsuleContract.Discoveries.TABLE_NAME;
            break;

        case CODE_OWNERSHIPS :
            subType = "dir";
            table = CapsuleContract.Ownerships.TABLE_NAME;
            break;

        case CODE_OWNERSHIPS_WILD :
            subType = "item";
            table = CapsuleContract.Ownerships.TABLE_NAME;
            break;

        default :
            throw new IllegalArgumentException("Unknown URI: " + uri);

        }

        return "vnd.android.cursor." + subType + "/vnd." + CapsuleContract.AUTHORITY + "/" + table;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri insertUri = null;
        long insertId = 0;
        mDb = mDbHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)) {

        case CODE_CAPSULES :
            insertId = mDb.insert(CapsuleContract.Capsules.TABLE_NAME, CapsuleContract.Capsules.NAME, values);

            if (insertId > 0) {
                insertUri = ContentUris.withAppendedId(CapsuleContract.Capsules.CONTENT_URI, insertId);
                getContext().getContentResolver().notifyChange(insertUri, null);
            }
            break;

        case CODE_DISCOVERIES :
            if (uri.getQueryParameter(CapsuleContract.QUERY_PARAM_TRANSACTION) != null
                && uri.getQueryParameter(CapsuleContract.QUERY_PARAM_TRANSACTION).equals(CapsuleContract.Capsules.TABLE_NAME)) {
                ContentValues capsuleValues = new ContentValues();
                capsuleValues.put(CapsuleContract.Capsules.SYNC_ID, values.getAsLong(CapsuleContract.Capsules.SYNC_ID));
                capsuleValues.put(CapsuleContract.Capsules.NAME, values.getAsString(CapsuleContract.Capsules.NAME));
                capsuleValues.put(CapsuleContract.Capsules.LATITUDE, values.getAsDouble(CapsuleContract.Capsules.LATITUDE));
                capsuleValues.put(CapsuleContract.Capsules.LONGITUDE, values.getAsDouble(CapsuleContract.Capsules.LONGITUDE));
                mDb.beginTransaction();
                try {
                    SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
                    // Check if a Capsule row exists for the unique sync ID
                    qb.setTables(CapsuleContract.Capsules.TABLE_NAME);
                    Cursor c = qb.query(
                            mDb,
                            new String[]{CapsuleContract.Capsules._ID},
                            CapsuleContract.Capsules.SYNC_ID + " = ?",
                            new String[]{String.valueOf(values.getAsLong(CapsuleContract.Capsules.SYNC_ID))},
                            null,
                            null,
                            null
                    );
                    // Check if the Capsule already exists and get its application specific primary key
                    long capsuleId;
                    if (c.getCount() < 1) {
                        // Does not exist, so insert it
                        capsuleId = mDb.insert(CapsuleContract.Capsules.TABLE_NAME, CapsuleContract.Capsules.NAME, capsuleValues);
                
                        if (capsuleId > 0) {
                            Uri newCapsuleUri = ContentUris.withAppendedId(CapsuleContract.Capsules.CONTENT_URI, capsuleId);
                            getContext().getContentResolver().notifyChange(newCapsuleUri, null);
                        }
                    } else {
                        c.moveToFirst();
                        capsuleId = c.getLong(c.getColumnIndex(CapsuleContract.Capsules._ID));
                    }
                    // Close the cursor
                    c.close();
                    // INSERT the Discovery
                    ContentValues discoveryValues = new ContentValues();
                    discoveryValues.put(CapsuleContract.Discoveries.CAPSULE_ID, capsuleId);
                    discoveryValues.put(CapsuleContract.Discoveries.ACCOUNT_NAME, values.getAsString(CapsuleContract.Discoveries.ACCOUNT_NAME));
                    if (uri.getQueryParameter(CapsuleContract.QUERY_PARAM_SET_DIRTY) != null
                            && uri.getQueryParameter(CapsuleContract.QUERY_PARAM_SET_DIRTY).equals(CapsuleContract.QUERY_VALUE_TRUE)) {
                        discoveryValues.put(CapsuleContract.Discoveries.DIRTY, 1);
                    }
                    long discoveryId = mDb.insert(CapsuleContract.Discoveries.TABLE_NAME, CapsuleContract.Discoveries.FAVORITE, discoveryValues);
                    if (discoveryId > 0) {
                        insertUri = ContentUris.withAppendedId(CapsuleContract.Discoveries.CONTENT_URI, discoveryId).buildUpon()
                                    .appendQueryParameter(CapsuleContract.Discoveries.CAPSULE_ID, String.valueOf(capsuleId))
                                    .build();
                        getContext().getContentResolver().notifyChange(insertUri, null);
                    }
                    // No exceptions, so flag to commit
                    mDb.setTransactionSuccessful();
                } finally {
                    // Commit
                    mDb.endTransaction();
                }
            } else {
                insertId = mDb.insert(CapsuleContract.Discoveries.TABLE_NAME, CapsuleContract.Discoveries.CAPSULE_ID, values);
    
                if (insertId > 0) {
                    insertUri = ContentUris.withAppendedId(CapsuleContract.Discoveries.CONTENT_URI, insertId);
                    getContext().getContentResolver().notifyChange(insertUri, null);
                }
            }
            break;

        case CODE_OWNERSHIPS :
            if (uri.getQueryParameter(CapsuleContract.QUERY_PARAM_TRANSACTION) != null
                && uri.getQueryParameter(CapsuleContract.QUERY_PARAM_TRANSACTION).equals(CapsuleContract.Capsules.TABLE_NAME)) {
                ContentValues capsuleValues = new ContentValues();
                capsuleValues.put(CapsuleContract.Capsules.SYNC_ID, 0);
                capsuleValues.put(CapsuleContract.Capsules.NAME, values.getAsString(CapsuleContract.Capsules.NAME));
                capsuleValues.put(CapsuleContract.Capsules.LATITUDE, values.getAsDouble(CapsuleContract.Capsules.LATITUDE));
                capsuleValues.put(CapsuleContract.Capsules.LONGITUDE, values.getAsDouble(CapsuleContract.Capsules.LONGITUDE));
                mDb.beginTransaction();
                try {
                    boolean commit = true;
                    // Does not exist, so insert it
                    long capsuleId = mDb.insert(CapsuleContract.Capsules.TABLE_NAME, CapsuleContract.Capsules.NAME, capsuleValues);
                
                    if (capsuleId > 0) {
                        Uri newCapsuleUri = ContentUris.withAppendedId(CapsuleContract.Capsules.CONTENT_URI, capsuleId);
                        getContext().getContentResolver().notifyChange(newCapsuleUri, null);
                    } else {
                        commit = false;
                    }
                    // INSERT the Ownership
                    ContentValues ownershipValues = new ContentValues();
                    ownershipValues.put(CapsuleContract.Ownerships.CAPSULE_ID, capsuleId);
                    ownershipValues.put(CapsuleContract.Ownerships.ACCOUNT_NAME, values.getAsString(CapsuleContract.Ownerships.ACCOUNT_NAME));
                    if (uri.getQueryParameter(CapsuleContract.QUERY_PARAM_SET_DIRTY) != null
                            && uri.getQueryParameter(CapsuleContract.QUERY_PARAM_SET_DIRTY).equals(CapsuleContract.QUERY_VALUE_TRUE)) {
                        ownershipValues.put(CapsuleContract.Ownerships.DIRTY, 1);
                    }
                    long ownershipId = mDb.insert(CapsuleContract.Ownerships.TABLE_NAME, CapsuleContract.Ownerships.DIRTY, ownershipValues);
                    if (ownershipId > 0) {
                        insertUri = ContentUris.withAppendedId(CapsuleContract.Ownerships.CONTENT_URI, ownershipId).buildUpon()
                                    .appendQueryParameter(CapsuleContract.Ownerships.CAPSULE_ID, String.valueOf(capsuleId))
                                    .build();
                        getContext().getContentResolver().notifyChange(insertUri, null);
                    }
                    // No exceptions, so flag to commit
                    if (commit) {
                        mDb.setTransactionSuccessful();
                    }
                } finally {
                    // Commit
                    mDb.endTransaction();
                }                
            } else {
                insertId = mDb.insert(CapsuleContract.Ownerships.TABLE_NAME, CapsuleContract.Ownerships.CAPSULE_ID, values);
                
                if (insertId > 0) {
                    insertUri = ContentUris.withAppendedId(CapsuleContract.Ownerships.CONTENT_URI, insertId);
                    getContext().getContentResolver().notifyChange(insertUri, null);
                }
            }

            break;

        default :
            throw new IllegalArgumentException("Unkown URI: " + uri);

        }

        return insertUri;
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new DatabaseHelper(
                getContext(),
                DATABASE_NAME,
                null,
                DATABASE_VERSION
        );
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String table;

        switch (sUriMatcher.match(uri)) {

        case CODE_CAPSULES :
            table = CapsuleContract.Capsules.TABLE_NAME;
            break;

        case CODE_CAPSULES_WILD :
            table = CapsuleContract.Capsules.TABLE_NAME;
            qb.appendWhere(CapsuleContract.Capsules._ID + " = " + uri.getPathSegments().get(PATH_ID_POS));
            break;

        case CODE_DISCOVERIES :
            if (uri.getQueryParameter(CapsuleContract.QUERY_PARAM_JOIN) != null
                && uri.getQueryParameter(CapsuleContract.QUERY_PARAM_JOIN).equals(CapsuleContract.Capsules.TABLE_NAME)) {
                table = CapsuleContract.Discoveries.TABLE_NAME + " INNER JOIN " + CapsuleContract.Capsules.TABLE_NAME
                        + " ON " + CapsuleContract.Discoveries.TABLE_NAME + "." + CapsuleContract.Discoveries.CAPSULE_ID
                        + " = " + CapsuleContract.Capsules.TABLE_NAME + "." + CapsuleContract.Capsules._ID;
            } else {
                table = CapsuleContract.Discoveries.TABLE_NAME;
            }
            break;

        case CODE_DISCOVERIES_WILD :
            table = CapsuleContract.Discoveries.TABLE_NAME;
            qb.appendWhere(CapsuleContract.Discoveries._ID + " = " + uri.getPathSegments().get(PATH_ID_POS));
            break;

        case CODE_OWNERSHIPS :
            if (uri.getQueryParameter(CapsuleContract.QUERY_PARAM_JOIN) != null
                && uri.getQueryParameter(CapsuleContract.QUERY_PARAM_JOIN).equals(CapsuleContract.Capsules.TABLE_NAME)) {
                table = CapsuleContract.Ownerships.TABLE_NAME + " INNER JOIN " + CapsuleContract.Capsules.TABLE_NAME
                        + " ON " + CapsuleContract.Ownerships.TABLE_NAME + "." + CapsuleContract.Ownerships.CAPSULE_ID
                        + " = " + CapsuleContract.Capsules.TABLE_NAME + "." + CapsuleContract.Capsules._ID;                
            } else {
                table = CapsuleContract.Ownerships.TABLE_NAME;
            }
            break;

        case CODE_OWNERSHIPS_WILD :
            table = CapsuleContract.Ownerships.TABLE_NAME;
            qb.appendWhere(CapsuleContract.Ownerships._ID + " = " + uri.getPathSegments().get(PATH_ID_POS));
            break;

        default :
            throw new IllegalArgumentException("Unknown URI: " + uri);

        }

        qb.setTables(table);

        mDb = mDbHelper.getWritableDatabase();

        Cursor c = qb.query(mDb, null, selection, selectionArgs, null, null, sortOrder);

        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String table;
        if (selection == null) {
            selection = "";
        }

        mDb = mDbHelper.getWritableDatabase();

        int count;

        switch (sUriMatcher.match(uri)) {

        case CODE_CAPSULES :
            table = CapsuleContract.Capsules.TABLE_NAME;
            count = mDb.update(table, values, selection, selectionArgs);
            break;

        case CODE_CAPSULES_WILD :
            table = CapsuleContract.Capsules.TABLE_NAME;
            String capsuleId = uri.getPathSegments().get(PATH_ID_POS);
            selection += ((!TextUtils.isEmpty(selection)) ? " AND " : "") + CapsuleContract.Capsules._ID + " = " + capsuleId;
            mDb.beginTransaction();
            try {
                // UPDATE the Capsule
                count = mDb.update(table, values, selection, selectionArgs);
                // UPDATE the Ownerships as dirty
                if (uri.getQueryParameter(CapsuleContract.QUERY_PARAM_SET_DIRTY) != null
                        && uri.getQueryParameter(CapsuleContract.QUERY_PARAM_SET_DIRTY).equals(CapsuleContract.QUERY_VALUE_TRUE)) {
                    values = new ContentValues();
                    values.put(CapsuleContract.Ownerships.DIRTY, 1);
                    mDb.update(
                            CapsuleContract.Ownerships.TABLE_NAME,
                            values,
                            CapsuleContract.Ownerships.CAPSULE_ID + " = ?",
                            new String[]{capsuleId}
                    );
                }
                // No exceptions, so flag to commit
                mDb.setTransactionSuccessful();
            } finally {
                // Commit
                mDb.endTransaction();
            }
            break;

        case CODE_DISCOVERIES :
            table = CapsuleContract.Discoveries.TABLE_NAME;
            count = mDb.update(table, values, selection, selectionArgs);
            break;

        case CODE_DISCOVERIES_WILD :
            table = CapsuleContract.Discoveries.TABLE_NAME;
            selection += ((!TextUtils.isEmpty(selection)) ? " AND " : "") + CapsuleContract.Discoveries._ID + " = " + uri.getPathSegments().get(PATH_ID_POS);
            if (uri.getQueryParameter(CapsuleContract.QUERY_PARAM_SET_DIRTY) != null
                    && uri.getQueryParameter(CapsuleContract.QUERY_PARAM_SET_DIRTY).equals(CapsuleContract.QUERY_VALUE_TRUE)) {
                values.put(CapsuleContract.Discoveries.DIRTY, 1);
            }
            count = mDb.update(table, values, selection, selectionArgs);
            break;

        case CODE_OWNERSHIPS :
            table = CapsuleContract.Ownerships.TABLE_NAME;
            count = mDb.update(table, values, selection, selectionArgs);
            break;

        case CODE_OWNERSHIPS_WILD :
            table = CapsuleContract.Ownerships.TABLE_NAME;
            selection += ((!TextUtils.isEmpty(selection)) ? " AND " : "") + CapsuleContract.Ownerships._ID + " = " + uri.getPathSegments().get(PATH_ID_POS);
            count = mDb.update(table, values, selection, selectionArgs);
            break;

        default :
            throw new IllegalArgumentException("Unknown URI: " + uri);

        }

        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

    protected static final class DatabaseHelper extends SQLiteOpenHelper {

        private Context mContext;

        public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
            super(context, name, factory, version);
            mContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // Create the capsules table
            db.execSQL("CREATE TABLE " + CapsuleContract.Capsules.TABLE_NAME + " ("
                    + CapsuleContract.Capsules._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                    + CapsuleContract.Capsules.SYNC_ID + " INTEGER NOT NULL,"
                    + CapsuleContract.Capsules.NAME + " TEXT NOT NULL,"
                    + CapsuleContract.Capsules.LATITUDE + " REAL NOT NULL,"
                    + CapsuleContract.Capsules.LONGITUDE + " REAL NOT NULL"
                    + ");"
            );
            // Create the ownerships table
            db.execSQL("CREATE TABLE " + CapsuleContract.Ownerships.TABLE_NAME + " ("
                    + CapsuleContract.Ownerships._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                    + CapsuleContract.Ownerships.CAPSULE_ID + " INTEGER NOT NULL,"
                    + CapsuleContract.Ownerships.ETAG + " TEXT DEFAULT NULL,"
                    + CapsuleContract.Ownerships.ACCOUNT_NAME + " TEXT NOT NULL,"
                    + CapsuleContract.Ownerships.DIRTY + " INTEGER NOT NULL DEFAULT 0,"
                    + CapsuleContract.Ownerships.DELETED + " INTEGER NOT NULL DEFAULT 0,"
                    + "FOREIGN KEY (" + CapsuleContract.Ownerships.CAPSULE_ID + ") REFERENCES "
                        + CapsuleContract.Capsules.TABLE_NAME + "(" + CapsuleContract.Capsules._ID + ") ON DELETE CASCADE ON UPDATE CASCADE"
                    + ");"
            );
            // Create the discoveries table
            db.execSQL("CREATE TABLE " + CapsuleContract.Discoveries.TABLE_NAME + " ("
                    + CapsuleContract.Discoveries._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                    + CapsuleContract.Discoveries.CAPSULE_ID + " INTEGER NOT NULL,"
                    + CapsuleContract.Discoveries.ETAG + " TEXT DEFAULT NULL,"
                    + CapsuleContract.Discoveries.ACCOUNT_NAME + " TEXT NOT NULL,"
                    + CapsuleContract.Discoveries.DIRTY + " INTEGER NOT NULL DEFAULT 0,"
                    + CapsuleContract.Discoveries.FAVORITE + " INTEGER NOT NULL DEFAULT 0,"
                    + CapsuleContract.Discoveries.RATING + " INTEGER NOT NULL DEFAULT 0,"
                    + "FOREIGN KEY (" + CapsuleContract.Discoveries.CAPSULE_ID + ") REFERENCES "
                        + CapsuleContract.Capsules.TABLE_NAME + "(" + CapsuleContract.Capsules._ID + ") ON DELETE CASCADE ON UPDATE CASCADE"
                    + ");"
            );
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Delete the whole database
            // TODO Consider incremental database upgrade
            mContext.deleteDatabase(DATABASE_NAME);

            // Recreate the database
            this.onCreate(db);
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            super.onOpen(db);
            // Enable foreign keys
            if (!db.isReadOnly()) {
                db.execSQL("PRAGMA foreign_keys = ON;");
            }
        }

    }
}
