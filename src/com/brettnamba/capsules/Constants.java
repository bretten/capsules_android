package com.brettnamba.capsules;

/**
 * General application-wide constants.
 * 
 * @author Brett Namba
 *
 */
public class Constants {

    /**
     * The application's account type.
     */
    public static final String ACCOUNT_TYPE = "com.brettnamba.capsules";

    /**
     * The authentication token type for this application.
     */
    public static final String AUTH_TOKEN_TYPE = "com.brettnamba.capsules";

    /**
     * The HTTP Host.
     */
    public static final String HOST = "192.168.0.120";

    /**
     * The base URL of the API.
     */
    public static final String BASE_URL = "http://" + Constants.HOST;

    /**
     * The URI for user authentication.
     */
    public static final String AUTH_URI = "/users/authenticate";

    /**
     * HTTP Authorization header.
     */
    public static final String AUTH_HEADER = "Authorization";

    /**
     * The name of the field storing the authentication token in the authentication response JSON body.
     */
    public static final String AUTH_TOKEN_RESPONSE_FIELD = "token";

}
