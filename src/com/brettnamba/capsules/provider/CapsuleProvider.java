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

import java.util.List;

public class CapsuleProvider extends ContentProvider {

    private static final String DATABASE_NAME = "capsules.db";

    private static final int DATABASE_VERSION = 1;

    private static final UriMatcher sUriMatcher;

    private static final int CODE_CAPSULES = 10;
    private static final int CODE_CAPSULES_ID = 11;
    private static final int CODE_DISCOVERIES = 20;
    private static final int CODE_DISCOVERIES_ID = 21;
    private static final int CODE_OWNERSHIPS = 30;
    private static final int CODE_OWNERSHIPS_ID = 31;

    private static final int PATH_ID_POS = 1;

    private SQLiteOpenHelper mDbHelper;

    private SQLiteDatabase mDb;

    private static final String TAG = "CapsuleProvider";

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(CapsuleContract.AUTHORITY, CapsuleContract.Capsules.CONTENT_URI_PATH, CODE_CAPSULES);
        sUriMatcher.addURI(CapsuleContract.AUTHORITY, CapsuleContract.Capsules.CONTENT_URI_PATH + "/#", CODE_CAPSULES_ID);
        sUriMatcher.addURI(CapsuleContract.AUTHORITY, CapsuleContract.Discoveries.CONTENT_URI_PATH, CODE_DISCOVERIES);
        sUriMatcher.addURI(CapsuleContract.AUTHORITY, CapsuleContract.Discoveries.CONTENT_URI_PATH + "/#", CODE_DISCOVERIES_ID);
        sUriMatcher.addURI(CapsuleContract.AUTHORITY, CapsuleContract.Ownerships.CONTENT_URI_PATH, CODE_OWNERSHIPS);
        sUriMatcher.addURI(CapsuleContract.AUTHORITY, CapsuleContract.Ownerships.CONTENT_URI_PATH + "/#", CODE_OWNERSHIPS_ID);
    }

    @Override
    public boolean onCreate() {
        this.mDbHelper = new DatabaseHelper(this.getContext(), DATABASE_NAME, null, DATABASE_VERSION);
        return true;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        String table;
        if (selection == null) {
            selection = "";
        }

        // Match the URI
        switch (sUriMatcher.match(uri)) {
            case CODE_CAPSULES:
                table = CapsuleContract.Capsules.TABLE_NAME;
                break;

            case CODE_CAPSULES_ID:
                table = CapsuleContract.Capsules.TABLE_NAME;
                selection += ((!TextUtils.isEmpty(selection)) ? " AND " : "") + CapsuleContract.Capsules._ID + " = " + uri.getPathSegments().get(PATH_ID_POS);
                break;

            case CODE_DISCOVERIES:
                table = CapsuleContract.Discoveries.TABLE_NAME;
                break;

            case CODE_DISCOVERIES_ID:
                table = CapsuleContract.Discoveries.TABLE_NAME;
                selection += ((!TextUtils.isEmpty(selection)) ? " AND " : "") + CapsuleContract.Discoveries._ID + " = " + uri.getPathSegments().get(PATH_ID_POS);
                break;

            case CODE_OWNERSHIPS:
                table = CapsuleContract.Ownerships.TABLE_NAME;
                break;

            case CODE_OWNERSHIPS_ID:
                table = CapsuleContract.Ownerships.TABLE_NAME;
                selection += ((!TextUtils.isEmpty(selection)) ? " AND " : "") + CapsuleContract.Ownerships._ID + " = " + uri.getPathSegments().get(PATH_ID_POS);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        // Opening the database connection (deferred until absolutely necessary)
        this.mDb = this.mDbHelper.getWritableDatabase();

        // Execute the DELETE
        int count = this.mDb.delete(table, selection, selectionArgs);
        // Notify the changes
        this.getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

    @Override
    public String getType(Uri uri) {
        String table;
        String subType;

        // Match the URI
        switch (sUriMatcher.match(uri)) {
            case CODE_CAPSULES:
                subType = "dir";
                table = CapsuleContract.Capsules.TABLE_NAME;
                break;

            case CODE_CAPSULES_ID:
                subType = "item";
                table = CapsuleContract.Capsules.TABLE_NAME;
                break;

            case CODE_DISCOVERIES:
                subType = "dir";
                table = CapsuleContract.Discoveries.TABLE_NAME;
                break;

            case CODE_DISCOVERIES_ID:
                subType = "item";
                table = CapsuleContract.Discoveries.TABLE_NAME;
                break;

            case CODE_OWNERSHIPS:
                subType = "dir";
                table = CapsuleContract.Ownerships.TABLE_NAME;
                break;

            case CODE_OWNERSHIPS_ID:
                subType = "item";
                table = CapsuleContract.Ownerships.TABLE_NAME;
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        return "vnd.android.cursor." + subType + "/vnd." + CapsuleContract.AUTHORITY + "/" + table;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        String table;
        String nullColumn;
        String dirtyColumn;
        Uri contentUri;

        // Match the URI
        switch (sUriMatcher.match(uri)) {
            case CODE_CAPSULES:
                table = CapsuleContract.Capsules.TABLE_NAME;
                nullColumn = CapsuleContract.Capsules.NAME;
                dirtyColumn = null;
                contentUri = CapsuleContract.Capsules.CONTENT_URI;
                break;

            case CODE_DISCOVERIES:
                table = CapsuleContract.Discoveries.TABLE_NAME;
                nullColumn = CapsuleContract.Discoveries.CAPSULE_ID;
                dirtyColumn = CapsuleContract.Discoveries.DIRTY;
                contentUri = CapsuleContract.Discoveries.CONTENT_URI;
                break;

            case CODE_OWNERSHIPS:
                table = CapsuleContract.Ownerships.TABLE_NAME;
                nullColumn = CapsuleContract.Ownerships.CAPSULE_ID;
                dirtyColumn = CapsuleContract.Ownerships.DIRTY;
                contentUri = CapsuleContract.Ownerships.CONTENT_URI;
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        // Check if there is a dirty column and that the URI contains the dirty query parameter
        String dirtyQueryParam = uri.getQueryParameter(CapsuleContract.Query.Parameters.SET_DIRTY);
        if (dirtyColumn != null && dirtyQueryParam != null) {
            // Set the dirty value in the ContentValues if it is a valid query parameter value
            if (dirtyQueryParam.equals(CapsuleContract.Query.Values.TRUE)) {
                values.put(dirtyColumn, true);
            } else if (dirtyQueryParam.equals(CapsuleContract.Query.Values.FALSE)) {
                values.put(dirtyColumn, false);
            }
        }

        // Open the database connection (deferred until absolutely necessary)
        this.mDb = this.mDbHelper.getWritableDatabase();

        // Execute the INSERT
        Uri insertUri = null;
        long insertId = this.mDb.insert(table, nullColumn, values);
        if (insertId > 0) {
            insertUri = ContentUris.withAppendedId(contentUri, insertId);
            this.getContext().getContentResolver().notifyChange(insertUri, null);
        }

        return insertUri;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String table;
        // Column to join on when joining with the Capsules table
        String capsuleJoinColumn = null;
        // Check if there are any joins
        List<String> innerJoins = uri.getQueryParameters(CapsuleContract.Query.Parameters.INNER_JOIN);

        // Match the URI
        switch (sUriMatcher.match(uri)) {
            case CODE_CAPSULES:
                table = CapsuleContract.Capsules.TABLE_NAME;
                break;

            case CODE_CAPSULES_ID:
                table = CapsuleContract.Capsules.TABLE_NAME;
                qb.appendWhere(CapsuleContract.Capsules._ID + " = " + uri.getPathSegments().get(PATH_ID_POS));
                break;

            case CODE_DISCOVERIES:
                table = CapsuleContract.Discoveries.TABLE_NAME;
                if (innerJoins != null && innerJoins.size() > 0) {
                    capsuleJoinColumn = CapsuleContract.Discoveries.CAPSULE_ID;
                }
                break;

            case CODE_DISCOVERIES_ID:
                table = CapsuleContract.Discoveries.TABLE_NAME;
                if (innerJoins != null && innerJoins.size() > 0) {
                    capsuleJoinColumn = CapsuleContract.Discoveries.CAPSULE_ID;
                }
                qb.appendWhere(CapsuleContract.Discoveries.TABLE_NAME + "." + CapsuleContract.Discoveries._ID
                        + " = " + uri.getPathSegments().get(PATH_ID_POS));
                break;

            case CODE_OWNERSHIPS:
                table = CapsuleContract.Ownerships.TABLE_NAME;
                if (innerJoins != null && innerJoins.size() > 0) {
                    capsuleJoinColumn = CapsuleContract.Ownerships.CAPSULE_ID;
                }
                break;

            case CODE_OWNERSHIPS_ID:
                table = CapsuleContract.Ownerships.TABLE_NAME;
                if (innerJoins != null && innerJoins.size() > 0) {
                    capsuleJoinColumn = CapsuleContract.Ownerships.CAPSULE_ID;
                }
                qb.appendWhere(CapsuleContract.Ownerships.TABLE_NAME + "." + CapsuleContract.Ownerships._ID
                        + " = " + uri.getPathSegments().get(PATH_ID_POS));
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        // Add any joins
        if (innerJoins != null && innerJoins.size() > 0) {
            for (String joinTable : innerJoins) {
                // Join with the Capsules table
                if (capsuleJoinColumn != null && joinTable.equals(CapsuleContract.Capsules.TABLE_NAME)) {
                    table = table + " INNER JOIN " + CapsuleContract.Capsules.TABLE_NAME
                            + " ON " + table + "." + capsuleJoinColumn
                            + " = " + CapsuleContract.Capsules.TABLE_NAME + "." + CapsuleContract.Capsules._ID;
                }
            }
        }

        // Set the table(s)
        qb.setTables(table);

        // Open the database connection (deferred until absolutely necessary)
        this.mDb = this.mDbHelper.getWritableDatabase();

        // Execute the query
        Cursor c = qb.query(this.mDb, projection, selection, selectionArgs, null, null, sortOrder);

        // Set the notification URI to watch for changes
        c.setNotificationUri(this.getContext().getContentResolver(), uri);

        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String table;
        String dirtyColumn;
        if (selection == null) {
            selection = "";
        }

        // Match the URI
        switch (sUriMatcher.match(uri)) {
            case CODE_CAPSULES:
                table = CapsuleContract.Capsules.TABLE_NAME;
                dirtyColumn = null;
                break;

            case CODE_CAPSULES_ID:
                table = CapsuleContract.Capsules.TABLE_NAME;
                dirtyColumn = null;
                String capsuleId = uri.getPathSegments().get(PATH_ID_POS);
                selection += ((!TextUtils.isEmpty(selection)) ? " AND " : "") + CapsuleContract.Capsules._ID + " = " + capsuleId;
                break;

            case CODE_DISCOVERIES:
                table = CapsuleContract.Discoveries.TABLE_NAME;
                dirtyColumn = CapsuleContract.Discoveries.DIRTY;
                break;

            case CODE_DISCOVERIES_ID:
                table = CapsuleContract.Discoveries.TABLE_NAME;
                dirtyColumn = CapsuleContract.Discoveries.DIRTY;
                String discoveryId = uri.getPathSegments().get(PATH_ID_POS);
                selection += ((!TextUtils.isEmpty(selection)) ? " AND " : "") + CapsuleContract.Discoveries._ID + " = " + discoveryId;
                break;

            case CODE_OWNERSHIPS:
                table = CapsuleContract.Ownerships.TABLE_NAME;
                dirtyColumn = CapsuleContract.Ownerships.DIRTY;
                break;

            case CODE_OWNERSHIPS_ID:
                table = CapsuleContract.Ownerships.TABLE_NAME;
                dirtyColumn = CapsuleContract.Ownerships.DIRTY;
                String ownershipId = uri.getPathSegments().get(PATH_ID_POS);
                selection += ((!TextUtils.isEmpty(selection)) ? " AND " : "") + CapsuleContract.Ownerships._ID + " = " + ownershipId;
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        // Check if there is a dirty column and that the URI contains the dirty query parameter
        String dirtyQueryParam = uri.getQueryParameter(CapsuleContract.Query.Parameters.SET_DIRTY);
        if (dirtyColumn != null && dirtyQueryParam != null) {
            // Set the dirty value in the ContentValues if it is a valid query parameter value
            if (dirtyQueryParam.equals(CapsuleContract.Query.Values.TRUE)) {
                values.put(dirtyColumn, true);
            } else if (dirtyQueryParam.equals(CapsuleContract.Query.Values.FALSE)) {
                values.put(dirtyColumn, false);
            }
        }

        // Open the database connection (deferred until absolutely necessary)
        this.mDb = this.mDbHelper.getWritableDatabase();

        // Execute the UPDATE
        int count = this.mDb.update(table, values, selection, selectionArgs);
        // Notify the change
        this.getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

    protected static final class DatabaseHelper extends SQLiteOpenHelper {

        private Context mContext;

        public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
            super(context, name, factory, version);
            DatabaseHelper.this.mContext = context;
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
            DatabaseHelper.this.mContext.deleteDatabase(DATABASE_NAME);

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
