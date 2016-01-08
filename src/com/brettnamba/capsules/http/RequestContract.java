package com.brettnamba.capsules.http;

public final class RequestContract {

    /**
     * Protocol of the server.
     */
    public static final String PROTOCOL = "http://";

    /**
     * The HTTP Host.
     */
    public static final String HOST = "192.168.0.250";

    /**
     * The base URL of the API.
     */
    public static final String BASE_URL = RequestContract.PROTOCOL + RequestContract.HOST;

    /**
     * HTTP Authorization header.
     */
    public static final String AUTH_HEADER = "Authorization";

    /**
     * Request URIs
     */
    public static final class Uri {

        /**
         * The URI for user authentication.
         */
        public static final String AUTH_URI = "/api/token";

        /**
         * The URI for user registration.
         */
        public static final String REGISTER_URI = "/api/user";

        /**
         * The URI for getting nearby undiscovered Capsules.
         */
        public static final String UNDISCOVERED_CAPSULES_URI = "/api/ping";

        /**
         * The URI for opening a Capsule.
         */
        public static final String OPEN_CAPSULE_URI = "/api/open";

        /**
         * The URI for requesting the Ownership collection ctag.
         */
        public static final String CTAG_OWNERSHIPS_URI = "/api/ctag/capsules";

        /**
         * The URI that handles single Capsules
         */
        public static final String OWNERSHIP_URI = "/api/capsule";

        /**
         * URI that gives the status of Ownership Capsules
         */
        public static final String OWNERSHIP_STATUS_URI = "/api/status/capsules";

        /**
         * URI that for REPORTing on Ownership Capsules
         */
        public static final String OWNERSHIP_REPORT_URI = "/api/report/capsules";

        /**
         * URI for a collection of Capsules
         */
        public static final String CAPSULES_URI = "/api/capsules";

        /**
         * URI for a collection of Discoveries
         */
        public static final String DISCOVERIES_URI = "/api/discoveries";

        /**
         * URI for validating a Capsule
         */
        public static final String VALIDATE_CAPSULE_URI = "/api/capsule/?validate=true";

        /**
         * URI for saving a Capsule
         */
        public static final String SAVE_CAPSULE_URI = "/api/capsule/?validate=false";

        /**
         * URI for a Memoir entity
         */
        public static final String MEMOIR_URI = "/api/memoir/";

    }

    /**
     * Field names for HTTP requests and responses
     */
    public static final class Field {

        /**
         * The key for a data object
         */
        public static final String DATA = "data";

        /**
         * The key for a collection of messages
         */
        public static final String MESSAGES = "messages";

        /**
         * The field name for sorting
         */
        public static final String SORT = "sort";

        /**
         * The field name for filtering
         */
        public static final String FILTER = "filter";

        /**
         * The field name for paging results
         */
        public static final String PAGE = "page";

        /**
         * The field name for search key terms
         */
        public static final String SEARCH = "search";

        /**
         * The name of the field storing the authentication token in the authentication response JSON body.
         */
        public static final String AUTH_TOKEN_RESPONSE = "token";

        /**
         * The name of the field storing the CTag
         */
        public static final String CTAG = "ctag";

        /**
         * The name of the field storing a username in a JSON response body.
         */
        public static final String USERNAME = "username";

        /**
         * The name of the field storing an e-mail address in a JSON response body.
         */
        public static final String EMAIL = "email";

        /**
         * The name of the field storing a password in a JSON response body.
         */
        public static final String PASSWORD = "password";

        /**
         * The name of the field storing a password confirmation in a JSON response body.
         */
        public static final String PASSWORD_CONFIRMATION = "confirm_password";

        /**
         * The key for a single Capsule
         */
        public static final String CAPSULE = "capsule";

        /**
         * The key for a Capsule entity
         */
        public static final String CAPSULE_ENTITY = "Capsule";

        /**
         * The key for a collection of Capsules
         */
        public static final String CAPSULE_COLLECTION = "capsules";

        /**
         * The field storing a Capsule's unique server-side identifier
         */
        public static final String CAPSULE_SYNC_ID = "id";

        /**
         * The field storing a Capsule's name.
         */
        public static final String CAPSULE_NAME = "name";

        /**
         * The field storing the Capsule's latitude.
         */
        public static final String CAPSULE_LATITUDE = "lat";

        /**
         * The field storing the Capsule's longitude.
         */
        public static final String CAPSULE_LONGITUDE = "lng";

        /**
         * The field storing the Capsule's total rating.
         */
        public static final String CAPSULE_RATING = "total_rating";

        /**
         * The field storing the Capsule's discovery count.
         */
        public static final String CAPSULE_DISCOVERY_COUNT = "discovery_count";

        /**
         * The field storing the Capsule's favorite count.
         */
        public static final String CAPSULE_FAVORITE_COUNT = "favorite_count";

        /**
         * The field for storing a Capsule's etag.
         */
        public static final String CAPSULE_ETAG = "etag";

        /**
         * The key for a single Discovery object
         */
        public static final String DISCOVERY = "discovery";

        /**
         * The key for a Discovery entity
         */
        public static final String DISCOVERY_ENTITY = "Discovery";

        /**
         * The key for a collection of Discovery objects
         */
        public static final String DISCOVERY_COLLECTION = "discoveries";

        /**
         * The key for the Discovery's unique server-side identifier
         */
        public static final String DISCOVERY_SYNC_ID = "id";

        /**
         * The field for a Discovery etag
         */
        public static final String DISCOVERY_ETAG = "etag";

        /**
         * The field that determines if a Discovery has been opened
         */
        public static final String DISCOVERY_OPENED = "opened";

        /**
         * The field for the Discovery favorite flag
         */
        public static final String DISCOVERY_FAVORITE = "favorite";

        /**
         * The field for the rating of the Discovery
         */
        public static final String DISCOVERY_RATING = "rating";

        /**
         * The key for a single Memoir object
         */
        public static final String MEMOIR = "memoir";

        /**
         * The key for a Memoir entity
         */
        public static final String MEMOIR_ENTITY = "Memoir";

        /**
         * The field for storing a Memoir's unique server-side identifier
         */
        public static final String MEMOIR_SYNC_ID = "id";

        /**
         * The field for the Memoir title
         */
        public static final String MEMOIR_TITLE = "title";

        /**
         * The field for the Memoir message
         */
        public static final String MEMOIR_MESSAGE = "message";

        /**
         * The field for the Memoir file
         */
        public static final String MEMOIR_FILE = "file";

        /**
         * The field name for a User entity
         */
        public static final String USER_ENTITY = "User";

        /**
         * The field name for a User's username
         */
        public static final String USER_USERNAME = "username";

    }

    /**
     * Pre-determined values for HTTP requests and responses
     */
    public static final class Value {

        /**
         * Sort key for sorting Capsules by name, A-Z
         */
        public static final int CAPSULE_SORT_NAME_ASC = 1;

        /**
         * Sort key for sorting Capsules from highest to lowest rating
         */
        public static final int CAPSULE_SORT_RATING_DESC = 2;

        /**
         * Sort key for sorting Capsules from most to least discoveries
         */
        public static final int CAPSULE_SORT_DISCOVERY_COUNT_DESC = 3;

        /**
         * Sort key for sorting Capsules from most to least favorites
         */
        public static final int CAPSULE_SORT_FAVORITE_COUNT_DESC = 4;

        /**
         * Filter key for filtering Capsules that have been set as favorites
         */
        public static final int CAPSULE_FILTER_FAVORITES = 1;

        /**
         * Filter key for filtering Capsules that have been rated up
         */
        public static final int CAPSULE_FILTER_UP_VOTES = 2;

        /**
         * Filter key for filtering Capsules that have been rated down
         */
        public static final int CAPSULE_FILTER_DOWN_VOTES = 3;

        /**
         * Filter key for filtering Capsules that have not been rated
         */
        public static final int CAPSULE_FILTER_NO_VOTES = 4;

        /**
         * Filter key for filtering Capsules that have not been opened
         */
        public static final int CAPSULE_FILTER_UNOPENED = 5;

    }

    /**
     * File upload contract
     */
    public static final class Upload {

        /**
         * The max file size for an image upload
         */
        public static final int MAX_IMAGE_FILE_SIZE = 5120000;

        /**
         * The max file size for an image upload in a human readable format.  This needs to be
         * kept in sync with MAX_IMAGE_FILE_SIZE, otherwise should determine the human
         * readable value dynamically.
         */
        public static final String MAX_IMAGE_FILE_SIZE_HUMAN = "5MB";

    }

}
