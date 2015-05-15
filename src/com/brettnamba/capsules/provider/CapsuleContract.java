package com.brettnamba.capsules.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Contract class containing constants for the CapsuleProvider
 */
public final class CapsuleContract {

    /**
     * Provider's authority string
     */
    public static final String AUTHORITY = "com.brettnamba.capsules.provider";

    /**
     * Scheme content
     */
    public static final String SCHEME = ContentResolver.SCHEME_CONTENT + "://";

    /**
     * Content URI
     */
    public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY);

    /**
     * Used to distinguish when an entity's sync state should be set as
     * dirty (un-synced), clean (synced), or should not be changed
     */
    public enum SyncStateAction {
        DIRTY,
        CLEAN,
        NONE
    }

    /**
     * Interface containing Capsule database columns names
     */
    protected interface CapsuleColumns {

        /**
         * Name of the column that stores Capsule's name
         */
        String NAME = "name";

        /**
         * Name of the column that stores the Capsule's latitude
         */
        String LATITUDE = "lat";

        /**
         * Name of the column that stores the Capsule's longitude
         */
        String LONGITUDE = "lng";

    }

    /**
     * Interface containing Ownership database columns names
     */
    protected interface OwnershipColumns {

        /**
         * Foreign key column that references the related Capsule
         */
        String CAPSULE_ID = "capsule_id";

        /**
         * Alias for the Ownership table's primary key
         */
        String OWNERSHIP_ID_ALIAS = "ownership_id";

    }

    /**
     * Interface containing Discovery database columns names
     */
    protected interface DiscoveryColumns {

        /**
         * Foreign key column that references the related Capsule
         */
        String CAPSULE_ID = "capsule_id";

        /**
         * Name of the column that stores favorite flag
         */
        String FAVORITE = "favorite";

        /**
         * Name of the column that stores the rating
         */
        String RATING = "rating";

        /**
         * Alias for the Discovery table's primary key
         */
        String DISCOVERY_ID_ALIAS = "discovery_id";

    }

    /**
     * Interface containing common database column names used for data syncing
     */
    protected interface SyncColumns {

        /**
         * Name of the column that stores the server's sync ID
         */
        String SYNC_ID = "sync_id";

        /**
         * Name of the column that stores the HTTP entity tag
         */
        String ETAG = "etag";

        /**
         * Name of the column that stores the dirty flag
         */
        String DIRTY = "dirty";

        /**
         * Name of the column that stores the deleted flag
         */
        String DELETED = "deleted";

        /**
         * Name of the column that stores the name of the Android Account that the row
         * belongs to
         */
        String ACCOUNT_NAME = "account_name";

    }

    /**
     * Implements all of the database columns for the Capsules table
     */
    public static final class Capsules implements BaseColumns, CapsuleColumns, SyncColumns {

        /**
         * Constructor
         */
        private Capsules() {
        }

        /**
         * The Capsules table name
         */
        public static final String TABLE_NAME = "capsules";

        /**
         * The name of the content URI path
         */
        public static final String CONTENT_URI_PATH = "capsules";

        /**
         * The Capsules content URI
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + "/" + CONTENT_URI_PATH);

    }

    /**
     * Implements all of the database columns for the Ownerships table
     */
    public static final class Ownerships implements BaseColumns, OwnershipColumns, SyncColumns {

        /**
         * Constructor
         */
        private Ownerships() {
        }

        /**
         * The Ownerships table name
         */
        public static final String TABLE_NAME = "ownerships";

        /**
         * The name of the Ownership content URI path
         */
        public static final String CONTENT_URI_PATH = "ownerships";

        /**
         * The Ownerships content URI
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + "/" + CONTENT_URI_PATH);

        /**
         * Basic projection for joining the Capsules table with the Ownerships table
         */
        public static final String[] CAPSULE_JOIN_PROJECTION = new String[]{
                TABLE_NAME + "." + _ID + " AS " + OWNERSHIP_ID_ALIAS,
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

    /**
     * Implements all of the database columns for the Discoveries table
     */
    public static final class Discoveries implements BaseColumns, DiscoveryColumns, SyncColumns {

        /**
         * Constructor
         */
        private Discoveries() {
        }

        /**
         * The Discoveries table name
         */
        public static final String TABLE_NAME = "discoveries";

        /**
         * The Discoveries content URI path
         */
        public static final String CONTENT_URI_PATH = "discoveries";

        /**
         * The Discoveries content URI
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + "/" + CONTENT_URI_PATH);

        /**
         * Basic projection for joining the Discoveries table with the Capsules table
         */
        public static final String[] CAPSULE_JOIN_PROJECTION = new String[]{
                TABLE_NAME + "." + _ID + " AS " + DISCOVERY_ID_ALIAS,
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

    /**
     * Defines constants for parameters and values when querying the Capsule ContentProvider
     */
    public static final class Query {

        /**
         * Constructor
         */
        private Query() {
        }

        /**
         * Defines query parameters
         */
        public static final class Parameters {
            /**
             * Constructor
             */
            private Parameters() {
            }

            /**
             * Query parameter to indicate an inner join
             */
            public static final String INNER_JOIN = "inner_join";

            /**
             * Query parameter that indicates when the dirty flag should be set or not
             */
            public static final String SET_DIRTY = "set_dirty";
        }

        /**
         * Defines query parameter values
         */
        public static final class Values {
            /**
             * Constructor
             */
            private Values() {
            }

            /**
             * Query parameter value for true
             */
            public static final String TRUE = "true";

            /**
             * Query parameter value for false
             */
            public static final String FALSE = "false";
        }

    }

}
