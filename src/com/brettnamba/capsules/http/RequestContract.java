package com.brettnamba.capsules.http;

public final class RequestContract {

    /**
     * Protocol of the server.
     */
    public static final String PROTOCOL = "http://";

    /**
     * The HTTP Host.
     */
    public static final String HOST = "192.168.0.120";

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
        public static final String AUTH_URI = "/users/authenticate";
    
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
    }

    /**
     * Field names for HTTP requests and responses
     */
    public static final class Field {

        /**
         * The name of the field storing the authentication token in the authentication response JSON body.
         */
        public static final String AUTH_TOKEN_RESPONSE = "token";

        /**
         * The field storing a Capsule object.
         */
        public static final String CAPSULE_OBJECT = "Capsule";

        /**
         * The field storing a Capsule's external primary key.
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
         * The field for storing a Capsule's etag.
         */
        public static final String CAPSULE_ETAG = "etag";
    }

}
