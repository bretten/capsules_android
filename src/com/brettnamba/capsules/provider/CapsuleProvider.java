package com.brettnamba.capsules.provider;

import java.util.List;
import java.util.Map;

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

    private static final int CODE_CAPSULES = 1;
    private static final int CODE_CAPSULES_WILD = 2;
    private static final int CODE_OWNERSHIPS = 3;
    private static final int CODE_OWNERSHIPS_WILD = 4;
    private static final int CODE_DISCOVERIES = 5;
    private static final int CODE_DISCOVERIES_WILD = 6;

    private static final int PATH_ID_POS = 1;

    private SQLiteOpenHelper mDbHelper;

    private SQLiteDatabase mDb;

    private static final String TAG = "CapsuleProvider";

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(CapsuleContract.AUTHORITY, CapsuleContract.Capsules.CONTENT_URI_PATH, CODE_CAPSULES);
        sUriMatcher.addURI(CapsuleContract.AUTHORITY, CapsuleContract.Capsules.CONTENT_URI_PATH + "/#", CODE_CAPSULES_WILD);
        sUriMatcher.addURI(CapsuleContract.AUTHORITY, CapsuleContract.Ownerships.CONTENT_URI_PATH, CODE_OWNERSHIPS);
        sUriMatcher.addURI(CapsuleContract.AUTHORITY, CapsuleContract.Ownerships.CONTENT_URI_PATH + "/#", CODE_OWNERSHIPS_WILD);
        sUriMatcher.addURI(CapsuleContract.AUTHORITY, CapsuleContract.Discoveries.CONTENT_URI_PATH, CODE_DISCOVERIES);
        sUriMatcher.addURI(CapsuleContract.AUTHORITY, CapsuleContract.Discoveries.CONTENT_URI_PATH + "/#", CODE_DISCOVERIES_WILD);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        String table;

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

        case CODE_OWNERSHIPS :
            insertId = mDb.insert(CapsuleContract.Ownerships.TABLE_NAME, null, values);

            if (insertId > 0) {
                insertUri = ContentUris.withAppendedId(CapsuleContract.Ownerships.CONTENT_URI, insertId);
                getContext().getContentResolver().notifyChange(insertUri, null);
            }
            break;

        case CODE_DISCOVERIES :
            insertId = mDb.insert(CapsuleContract.Discoveries.TABLE_NAME, null, values);

            if (insertId > 0) {
                insertUri = ContentUris.withAppendedId(CapsuleContract.Discoveries.CONTENT_URI, insertId);
                getContext().getContentResolver().notifyChange(insertUri, null);
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
            table = CapsuleContract.Discoveries.TABLE_NAME;
            break;

        case CODE_DISCOVERIES_WILD :
            table = CapsuleContract.Discoveries.TABLE_NAME;
            qb.appendWhere(CapsuleContract.Discoveries._ID + " = " + uri.getPathSegments().get(PATH_ID_POS));
            break;

        case CODE_OWNERSHIPS :
            table = CapsuleContract.Ownerships.TABLE_NAME;
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

        int count = mDb.update(table, values, selection, selectionArgs);

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
            db.execSQL(this.buildCreateSql(
                    CapsuleContract.Capsules.TABLE_NAME,
                    CapsuleContract.Capsules.COLUMN_INDEX_LIST,
                    CapsuleContract.Capsules.COLUMN_TYPE_MAP
            ));
            // Create the ownerships table
            db.execSQL(this.buildCreateSql(
                    CapsuleContract.Ownerships.TABLE_NAME,
                    CapsuleContract.Ownerships.COLUMN_INDEX_LIST,
                    CapsuleContract.Ownerships.COLUMN_TYPE_MAP
            ));
            // Create the discoveries table
            db.execSQL(this.buildCreateSql(
                    CapsuleContract.Discoveries.TABLE_NAME,
                    CapsuleContract.Discoveries.COLUMN_INDEX_LIST,
                    CapsuleContract.Discoveries.COLUMN_TYPE_MAP
            ));
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Delete the whole database
            // TODO Consider incremental database upgrade
            mContext.deleteDatabase(DATABASE_NAME);

            // Recreate the database
            this.onCreate(db);
        }

        /**
         * Builds a CREATE TABLE statement given the table name, columns and column types.
         * 
         * @param tableName
         * @param columnIndexList
         * @param columnTypeMap
         * @return String
         */
        private String buildCreateSql(String tableName, List<String> columnIndexList, Map<String, String> columnTypeMap) {
            String sql = "CREATE TABLE " + tableName + "(";
            // Append each column and its corresponding data type
            for (int i = 0; i < columnIndexList.size(); i++) {
                sql += columnIndexList.get(i) + " " + columnTypeMap.get(columnIndexList.get(i));
                if (i < columnIndexList.size() - 1) {
                    sql += ",";
                }
            }
            sql += ");";

            return sql;
        }

    }
}
