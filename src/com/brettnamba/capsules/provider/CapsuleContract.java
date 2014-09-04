package com.brettnamba.capsules.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class CapsuleContract {

    public static final String TAG = "Capsule";

    public static final String AUTHORITY = "com.brettnamba.capsules.provider";

    public static final String SCHEME = ContentResolver.SCHEME_CONTENT + "://";

    public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY);

    public static final String QUERY_PARAM_JOIN = "join";

    public static final String QUERY_PARAM_TRANSACTION = "transaction";

    public static final String QUERY_PARAM_SET_DIRTY = "set_dirty";

    public static final String QUERY_VALUE_TRUE = "true";

    protected interface CapsuleColumns {

        public static final String NAME = "name";

        public static final String LATITUDE = "lat";

        public static final String LONGITUDE = "lng";

    }

    protected interface OwnershipColumns {

        public static final String OWNERSHIP_ID = "ownership_id";

        public static final String CAPSULE_ID = "capsule_id";

    }

    protected interface DiscoveryColumns {

        public static final String DISCOVERY_ID = "discovery_id";

        public static final String CAPSULE_ID = "capsule_id";

        public static final String FAVORITE = "favorite";

        public static final String RATING = "rating";

    }

    protected interface SyncColumns {

        public static final String SYNC_ID = "sync_id";

        public static final String ETAG = "etag";

        public static final String DIRTY = "dirty";

        public static final String DELETED = "deleted";

        public static final String ACCOUNT_NAME = "account_name";

    }

    public static final class Capsules implements BaseColumns, CapsuleColumns, SyncColumns {

        private Capsules() {}

        public static final String TABLE_NAME = "capsules";

        public static final String CONTENT_URI_PATH = "capsules";

        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + "/" + CONTENT_URI_PATH);

    }

    public static final class Ownerships implements BaseColumns, OwnershipColumns, SyncColumns {

        private Ownerships() {}

        public static final String TABLE_NAME = "ownerships";

        public static final String CONTENT_URI_PATH = "ownerships";

        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + "/" + CONTENT_URI_PATH);

        public static final String[] CAPSULE_JOIN_PROJECTION = new String[] {
            TABLE_NAME + "." + _ID + " AS " + OWNERSHIP_ID,
            CAPSULE_ID,
            ETAG,
            ACCOUNT_NAME,
            DIRTY,
            DELETED,
            Capsules.TABLE_NAME + "." + Capsules._ID,
            Capsules.SYNC_ID,
            Capsules.NAME,
            Capsules.LATITUDE,
            Capsules.LONGITUDE
        };

    }

    public static final class Discoveries implements BaseColumns, DiscoveryColumns, SyncColumns {

        private Discoveries() {}

        public static final String TABLE_NAME = "discoveries";

        public static final String CONTENT_URI_PATH = "discoveries";

        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + "/" + CONTENT_URI_PATH);

        public static final String[] CAPSULE_JOIN_PROJECTION = new String[] {
            TABLE_NAME + "." + _ID + " AS " + DISCOVERY_ID,
            CAPSULE_ID,
            ETAG,
            ACCOUNT_NAME,
            DIRTY,
            FAVORITE,
            RATING,
            Capsules.TABLE_NAME + "." + Capsules._ID,
            Capsules.SYNC_ID,
            Capsules.NAME,
            Capsules.LATITUDE,
            Capsules.LONGITUDE
        };

    }

}
