package com.brettnamba.capsules.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class CapsuleContract {

    public static final String TAG = "Capsule";

    public static final String AUTHORITY = "com.brettnamba.capsules.provider";

    public static final String SCHEME = ContentResolver.SCHEME_CONTENT + "://";

    public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY);

    protected interface CapsuleColumns {

        public static final String NAME = "name";

        public static final String LATITUDE = "lat";

        public static final String LONGITUDE = "lng";

    }

    protected interface OwnershipColumns {

        public static final String CAPSULE_ID = "capsule_id";

    }

    protected interface DiscoveryColumns {

        public static final String CAPSULE_ID = "capsule_id";

        public static final String FAVORITE = "favorite";

        public static final String RATING = "rating";

    }

    protected interface SyncColumns {

        public static final String SYNC_ID = "sync_id";

        public static final String DIRTY = "dirty";

        public static final String ACCOUNT_NAME = "account_name";

    }

    public static final class Capsules implements BaseColumns, CapsuleColumns, SyncColumns {

        private Capsules() {}

        public static final String TABLE_NAME = "capsules";

        public static final String CONTENT_URI_PATH = "capsules";

        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + "/" + CONTENT_URI_PATH);

        /**
         * An ordered list of the database columns where their index in the List represents their database column index.
         */
        public static final List<String> COLUMN_INDEX_LIST;

        /**
         * A mapping of database columns to their column type declarations.
         */
        public static final Map<String, String> COLUMN_TYPE_MAP;

        static {
            List<String> columnIndexList = new ArrayList<String>();
            columnIndexList.add(_ID);
            columnIndexList.add(SYNC_ID);
            columnIndexList.add(NAME);
            columnIndexList.add(LATITUDE);
            columnIndexList.add(LONGITUDE);
            COLUMN_INDEX_LIST = Collections.unmodifiableList(columnIndexList);

            Map<String, String> columnTypeMap = new HashMap<String, String>();
            columnTypeMap.put(_ID, "INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL");
            columnTypeMap.put(SYNC_ID, "INTEGER NOT NULL");
            columnTypeMap.put(NAME, "TEXT NOT NULL");
            columnTypeMap.put(LATITUDE, "REAL NOT NULL");
            columnTypeMap.put(LONGITUDE, "REAL NOT NULL");
            COLUMN_TYPE_MAP = Collections.unmodifiableMap(columnTypeMap);
        }

    }

    public static final class Ownerships implements BaseColumns, OwnershipColumns, SyncColumns {

        private Ownerships() {}

        public static final String TABLE_NAME = "ownerships";

        public static final String CONTENT_URI_PATH = "ownerships";

        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + "/" + CONTENT_URI_PATH);

        /**
         * An ordered list of the database columns where their index in the List represents their database column index.
         */
        public static final List<String> COLUMN_INDEX_LIST;

        /**
         * A mapping of database columns to their column type declarations.
         */
        public static final Map<String, String> COLUMN_TYPE_MAP;

        static {
            List<String> columnIndexList = new ArrayList<String>();
            columnIndexList.add(_ID);
            columnIndexList.add(CAPSULE_ID);
            columnIndexList.add(ACCOUNT_NAME);
            columnIndexList.add(DIRTY);
            COLUMN_INDEX_LIST = Collections.unmodifiableList(columnIndexList);

            Map<String, String> columnTypeMap = new HashMap<String, String>();
            columnTypeMap.put(_ID, "INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL");
            columnTypeMap.put(CAPSULE_ID, "INTEGER NOT NULL");
            columnTypeMap.put(ACCOUNT_NAME, "TEXT NOT NULL");
            columnTypeMap.put(DIRTY, "INTEGER NOT NULL DEFAULT 0");
            COLUMN_TYPE_MAP = Collections.unmodifiableMap(columnTypeMap);
        }

    }

    public static final class Discoveries implements BaseColumns, DiscoveryColumns, SyncColumns {

        private Discoveries() {}

        public static final String TABLE_NAME = "discoveries";

        public static final String CONTENT_URI_PATH = "discoveries";

        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + "/" + CONTENT_URI_PATH);

        /**
         * An ordered list of the database columns where their index in the List represents their database column index.
         */
        public static final List<String> COLUMN_INDEX_LIST;

        /**
         * A mapping of database columns to their column type declarations.
         */
        public static final Map<String, String> COLUMN_TYPE_MAP;

        static {
            List<String> columnIndexList = new ArrayList<String>();
            columnIndexList.add(_ID);
            columnIndexList.add(CAPSULE_ID);
            columnIndexList.add(ACCOUNT_NAME);
            columnIndexList.add(DIRTY);
            columnIndexList.add(FAVORITE);
            columnIndexList.add(RATING);
            COLUMN_INDEX_LIST = Collections.unmodifiableList(columnIndexList);

            Map<String, String> columnTypeMap = new HashMap<String, String>();
            columnTypeMap.put(_ID, "INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL");
            columnTypeMap.put(CAPSULE_ID, "INTEGER NOT NULL");
            columnTypeMap.put(ACCOUNT_NAME, "TEXT NOT NULL");
            columnTypeMap.put(DIRTY, "INTEGER NOT NULL DEFAULT 0");
            columnTypeMap.put(FAVORITE, "INTEGER NOT NULL DEFAULT 0");
            columnTypeMap.put(RATING, "INTEGER NOT NULL DEFAULT 0");
            COLUMN_TYPE_MAP = Collections.unmodifiableMap(columnTypeMap);
        }
    }

}
