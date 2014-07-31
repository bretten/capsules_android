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
        public static final String UNDISCOVERED_CAPSULES_URI = "/capsules/ping";

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

    }

}
