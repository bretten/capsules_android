package com.brettnamba.capsules.http;

import android.accounts.Account;
import android.content.Context;

import org.apache.http.client.methods.HttpPost;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;

/**
 * Implementation of HttpUrlConnectionRequest that provides functionality for sending a
 * HTTP request whose type is "application/x-www-form-urlencoded".
 *
 * @author Brett Namba
 */
public class HttpUrlWwwFormRequest extends HttpUrlConnectionRequest {

    /**
     * Bytes representing the URL encoded HTTP request parameters
     */
    protected byte[] mRequestParameterBytes;

    /**
     * Constructs an instance only with the request URL
     *
     * @param context    The current Context
     * @param requestUrl The HTTP request URL
     */
    public HttpUrlWwwFormRequest(Context context, String requestUrl) {
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
    public HttpUrlWwwFormRequest(Context context, String requestUrl, Account account,
                                 String authTokenType) {
        super(context, requestUrl, account, authTokenType);
    }

    /**
     * Sets up the HTTP request by specifying the Content-Type header as
     * "application/x-www-form-urlencoded" and also converts the request parameters to bytes
     *
     * @param httpUrlConnection The HTTP request object that will be setup
     * @throws ProtocolException
     */
    @Override
    protected void setupRequest(HttpURLConnection httpUrlConnection) throws ProtocolException {
        // The request method is POST for application/x-www-form-urlencoded requests
        httpUrlConnection.setRequestMethod(HttpPost.METHOD_NAME);
        // Set the content type as multipart form data
        this.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        // Convert the request parameters and file upload request body headers to bytes
        this.convertRequestParametersToBytes();
    }

    /**
     * Determines the content length of the request body which is simply the byte count of the
     * URL encoded request parameters
     *
     * @return The content-length of the request body
     */
    @Override
    protected long determineRequestBodyLength() {
        return this.mTotalRequestParameterByteCount;
    }

    /**
     * Writes the request parameters bytes to the HTTP request stream
     */
    @Override
    protected void writeToRequestStream() {
        try {
            // Write the final boundary to the stream
            this.mRequestStream.write(this.mRequestParameterBytes);
            // Notify the listener tracking the amount of data sent
            this.notifyDataSentListener(this.mRequestParameterBytes.length,
                    this.mRequestBodyLength);
        } catch (IOException e) {
        }
    }

    /**
     * Converts the collection of request parameters to bytes and determines the total byte count
     * of all the request parameters
     */
    protected void convertRequestParametersToBytes() {
        try {
            // Build the request body
            String requestBody = this.urlEncodeParameters(this.mRequestParameters);
            // Get the bytes from the request body
            this.mRequestParameterBytes = requestBody.getBytes();
            // Get the total length of the bytes
            this.mTotalRequestParameterByteCount = this.mRequestParameterBytes.length;
        } catch (UnsupportedEncodingException e) {
        }
    }

}
