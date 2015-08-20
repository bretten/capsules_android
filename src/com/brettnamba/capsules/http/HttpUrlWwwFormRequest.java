package com.brettnamba.capsules.http;

import android.accounts.Account;
import android.content.Context;
import android.support.v4.util.Pair;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URLEncoder;
import java.util.List;

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
     * Constructs an instance without authentication information
     *
     * @param context       The current Context
     * @param requestMethod The HTTP request method
     * @param requestUrl    The HTTP request URL
     */
    public HttpUrlWwwFormRequest(Context context, String requestMethod, String requestUrl) {
        super(context, requestMethod, requestUrl);
    }

    /**
     * Constructs an instance with authentication information and adds the authentication header to
     * the collection of request headers
     *
     * @param context       The current Context
     * @param requestMethod The HTTP request method
     * @param requestUrl    The HTTP request URL
     * @param account       The Account that will be used to get the authentication token
     * @param authTokenType The type of authentication token
     */
    public HttpUrlWwwFormRequest(Context context, String requestMethod, String requestUrl,
                                 Account account, String authTokenType) {
        super(context, requestMethod, requestUrl, account, authTokenType);
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
            this.notifyDataSentListener(this.mRequestParameterBytes.length, this.mRequestBodyLength);
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

    /**
     * URL encodes all the request parameters and builds the request body string
     *
     * @param requestParameters The collection of request parameters to add to the request body
     * @return The full URL encoded representation of all the request parameters
     * @throws UnsupportedEncodingException
     */
    private String urlEncodeParameters(List<Pair<String, String>> requestParameters) throws UnsupportedEncodingException {
        StringBuilder stringBuilder = new StringBuilder();
        boolean isFirst = true;

        for (Pair<String, String> requestParameter : requestParameters) {
            // See if an ampersand needs to be added
            if (isFirst) {
                isFirst = false;
            } else {
                stringBuilder.append("&");
            }

            stringBuilder.append(URLEncoder.encode(requestParameter.first, HTTP.UTF_8));
            stringBuilder.append("=");
            stringBuilder.append(URLEncoder.encode(requestParameter.second, HTTP.UTF_8));
        }

        return stringBuilder.toString();
    }

}
