package com.brettnamba.capsules.http;

import android.accounts.Account;
import android.content.Context;

import org.apache.http.client.methods.HttpGet;

import java.net.HttpURLConnection;
import java.net.ProtocolException;

/**
 * Implementation of HttpUrlConnectionRequest that sends a simple GET request.
 *
 * @author Brett Namba
 */
public class HttpUrlGetRequest extends HttpUrlConnectionRequest {

    /**
     * Constructs an instance only with the request URL
     *
     * @param context    The current Context
     * @param requestUrl The HTTP request URL
     */
    public HttpUrlGetRequest(Context context, String requestUrl) {
        super(context, requestUrl);
    }

    /**
     * Constructs an instance only with the request URL and authentication header
     *
     * @param context       The current Context
     * @param requestUrl    The HTTP request URL
     * @param account       The Account that will be used to get the authentication token
     * @param authTokenType The type of authentication token
     */
    public HttpUrlGetRequest(Context context, String requestUrl, Account account,
                             String authTokenType) {
        super(context, requestUrl, account, authTokenType);
    }

    /**
     * Sets up the request
     *
     * @param httpUrlConnection The HTTP request object that will be setup
     * @throws ProtocolException
     */
    @Override
    protected void setupRequest(HttpURLConnection httpUrlConnection) throws ProtocolException {
        // Set request method to GET
        httpUrlConnection.setRequestMethod(HttpGet.METHOD_NAME);
    }

    /**
     * Determines the request body length, which is 0 in the case
     *
     * @return 0
     */
    @Override
    protected long determineRequestBodyLength() {
        return 0;
    }

    /**
     * This GET request does not need a request body, so the method is not implemented
     */
    @Override
    protected void writeToRequestStream() {
        throw new UnsupportedOperationException();
    }

}
