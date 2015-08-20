package com.brettnamba.capsules.http;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.os.Build;
import android.support.v4.util.Pair;
import android.util.Base64;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract wrapper for HttpUrlConnection that simplifies sending HTTP requests.
 *
 * @author Brett Namba
 */
public abstract class HttpUrlConnectionRequest {

    /**
     * The current Context
     */
    protected Context mContext;

    /**
     * The Account used to retrieve the authentication token to be sent in the HTTP authentication
     * header
     */
    protected Account mAccount;

    /**
     * The type of authentication token associated to the Account
     */
    protected String mAuthTokenType;

    /**
     * Listener that receives updates whenever part of the HTTP request body is sent
     */
    protected DataSentListener mListener;

    /**
     * The HTTP object that backs the request
     */
    protected HttpURLConnection mHttpUrlConnection;

    /**
     * The HTTP request method
     */
    protected String mRequestMethod;

    /**
     * The HTTP request URL
     */
    protected String mRequestUrl;

    /**
     * Collection of HTTP request headers to be added to the request
     */
    protected List<Pair<String, String>> mRequestHeaders;

    /**
     * Collection of HTTP request parameters that implementing classes will decide how to use
     */
    protected List<Pair<String, String>> mRequestParameters;

    /**
     * The total byte count of the request parameters and meant to be used in calculating the
     * total content length of the request body
     */
    protected long mTotalRequestParameterByteCount = 0;

    /**
     * The stream used for writing the HTTP request data
     */
    protected OutputStream mRequestStream;

    /**
     * The stream used for writing the HTTP response data
     */
    protected InputStream mResponseStream;

    /**
     * The HTTP response body as a String
     */
    protected String mResponseBodyString;

    /**
     * The content length of the HTTP request body
     */
    protected long mRequestBodyLength;

    /**
     * The User-Agent associated with this app
     */
    protected static final String USER_AGENT = "capsules/1.0 (android)";

    /**
     * Determines if the request was a success or not
     */
    private boolean mIsSuccess;

    /**
     * Protected constructor to prevent parameter-less instantiation. Should only be called by
     * other constructors
     */
    protected HttpUrlConnectionRequest() {
        this.mRequestHeaders = new ArrayList<Pair<String, String>>();
        // Add default HTTP headers
        this.addDefaultRequestHeaders();
    }

    /**
     * Constructs an instance without authentication information
     *
     * @param context       The current Context
     * @param requestMethod The HTTP request method
     * @param requestUrl    The HTTP request URL
     */
    public HttpUrlConnectionRequest(Context context, String requestMethod, String requestUrl) {
        this();
        this.mContext = context;
        this.mRequestMethod = requestMethod;
        this.mRequestUrl = requestUrl;
        this.mRequestParameters = new ArrayList<Pair<String, String>>();
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
    public HttpUrlConnectionRequest(Context context, String requestMethod, String requestUrl,
                                    Account account, String authTokenType) {
        this(context, requestMethod, requestUrl);
        this.mAccount = account;
        this.mAuthTokenType = authTokenType;
        // Add the auth header
        this.addAuthHeader(this.mAuthTokenType, this.mAccount);
    }

    /**
     * Sets the DataSentListener which is used to listen for the event of writing data to the
     * output HTTP request stream
     *
     * @param dataSentListener Object that will listen for data being sent over the wire
     */
    public void setListener(DataSentListener dataSentListener) {
        this.mListener = dataSentListener;
    }

    /**
     * Adds an HTTP request header
     *
     * @param header The request header
     * @param value  The request value
     */
    public void addRequestHeader(String header, String value) {
        if (header == null || value == null) {
            throw new IllegalArgumentException("Header or value cannot be null");
        }
        this.mRequestHeaders.add(new Pair<String, String>(header, value));
    }

    /**
     * Adds an HTTP request parameter
     *
     * @param parameter The request parameter name
     * @param value     The request parameter value
     */
    public void addRequestParameter(String parameter, String value) {
        if (parameter == null || value == null) {
            throw new IllegalArgumentException("Parameter or value cannot be null");
        }
        this.mRequestParameters.add(new Pair<String, String>(parameter, value));
    }

    /**
     * Determines if the request is a success
     *
     * @return True if the request was successful, otherwise false
     */
    public boolean isSuccess() {
        return this.mIsSuccess;
    }

    /**
     * Returns the HTTP response body as a String
     *
     * @return The HTTP response body string
     */
    public String getResponseBody() {
        return this.mResponseBodyString;
    }

    /**
     * Instantiates the HTTP request object, setups the the request properties, determines
     * the content length of the request, sends the request, and parses the response
     */
    public void send() {
        try {
            // Instantiate the HttpUrlConnection
            final URL url = new URL(this.mRequestUrl);
            this.mHttpUrlConnection = (HttpURLConnection) url.openConnection();

            // Setup the options on the request
            this.setupRequest(this.mHttpUrlConnection);

            // Set the request headers to the connection object
            this.addRequestHeadersToConnection(this.mRequestHeaders);

            // Determine the content length of the request body
            this.mRequestBodyLength = this.determineRequestBodyLength();

            // Set the pre-determined content length
            if (Build.VERSION.SDK_INT >= 19) {
                this.mHttpUrlConnection.setFixedLengthStreamingMode(this.mRequestBodyLength);
            } else {
                this.mHttpUrlConnection.setFixedLengthStreamingMode((int) this.mRequestBodyLength);
            }

            // Get the stream for the request
            this.mRequestStream = this.mHttpUrlConnection.getOutputStream();

            // Write to the request stream
            this.writeToRequestStream();

            // Get the HTTP response code
            final int responseCode = this.mHttpUrlConnection.getResponseCode();

            // Check if the response code was in the success range
            if (responseCode >= 200 && responseCode <= 299) {
                this.mResponseStream = this.mHttpUrlConnection.getInputStream();
                this.mIsSuccess = true;
            } else {
                this.mResponseStream = this.mHttpUrlConnection.getErrorStream();
                this.mIsSuccess = false;
            }

            // Get the response body from the response stream
            this.mResponseBodyString = this.buildResponseBody(this.mResponseStream);
        } catch (IOException e) {
        } finally {
            this.closeOutputStream(this.mRequestStream);
            this.closeInputStream(this.mResponseStream);
            this.closeHttpConnection(this.mHttpUrlConnection);
        }
    }

    /**
     * Implementing classes should use this method to set up any case specific properties on the
     * HTTP request object
     *
     * @param httpUrlConnection The HTTP request object that will be setup
     * @throws ProtocolException
     */
    protected abstract void setupRequest(HttpURLConnection httpUrlConnection) throws ProtocolException;

    /**
     * Determines the content length of the HTTP request body.  Implementing classes should
     * consider the HTTP request body as a whole when calculating this.
     *
     * @return The content length of the HTTP request body
     */
    protected abstract long determineRequestBodyLength();

    /**
     * Writes the HTTP request stream
     */
    protected abstract void writeToRequestStream();

    /**
     * Adds HTTP headers to the collection that will remain the same for all implementing classes
     */
    protected void addDefaultRequestHeaders() {
        // User agent
        this.mRequestHeaders.add(new Pair<String, String>("User-Agent",
                HttpUrlConnectionRequest.USER_AGENT));
    }

    /**
     * Using the authentication token type and Android Account, will retrieve the authentication
     * token and add it to the request header collection as the authentication header
     *
     * @param authTokenType The authentication token type
     * @param account       The Account used to retrieve the authentication token
     */
    protected void addAuthHeader(String authTokenType, Account account) {
        if (account != null) {
            try {
                // Get the token
                final String authToken = AccountManager.get(this.mContext).blockingGetAuthToken(
                        account, authTokenType, true);
                // Add the header
                this.addRequestHeader(RequestContract.AUTH_HEADER, Base64.encodeToString(
                        (authToken).getBytes(), Base64.URL_SAFE | Base64.NO_WRAP));
            } catch (OperationCanceledException | IOException | AuthenticatorException e) {
            }
        }
    }

    /**
     * Notifies the DataSentListener the number of bytes that were just uploaded and the total
     * number of bytes it needs to upload
     *
     * @param bytesUploaded The number of bytes just uploaded
     * @param totalBytes    The total number of bytes for the whole request body
     */
    protected void notifyDataSentListener(long bytesUploaded, long totalBytes) {
        if (this.mListener != null) {
            this.mListener.onDataSent(bytesUploaded, totalBytes);
        }
    }

    /**
     * Takes each HTTP request header from the collection and sets them on the HTTP request object
     *
     * @param requestHeaders The HTTP request header collection
     */
    private void addRequestHeadersToConnection(List<Pair<String, String>> requestHeaders) {
        for (Pair<String, String> requestHeader : requestHeaders) {
            this.mHttpUrlConnection.setRequestProperty(requestHeader.first, requestHeader.second);
        }
    }

    /**
     * Reads the HTTP response stream and builds a String representing the HTTP response body
     *
     * @param stream The HTTP response stream
     * @return The HTTP response body as a String
     * @throws IOException
     */
    private String buildResponseBody(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder responseBodyBuilder = new StringBuilder();
        String line;
        do {
            line = reader.readLine();
            if (line != null) {
                responseBodyBuilder.append(line);
            }
        } while (line != null);

        return responseBodyBuilder.toString();
    }

    /**
     * Closes an InputStream
     *
     * @param inputStream The InputStream that will be closed
     */
    protected void closeInputStream(InputStream inputStream) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * Closes an OutputStream
     *
     * @param outputStream The OutputStream that will be closed
     */
    protected void closeOutputStream(OutputStream outputStream) {
        if (outputStream != null) {
            try {
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * Closes an HttpUrlConnection
     *
     * @param httpURLConnection The HttpUrlConnection that will be closed
     */
    protected void closeHttpConnection(HttpURLConnection httpURLConnection) {
        if (httpURLConnection != null) {
            httpURLConnection.disconnect();
        }
    }

    /**
     * Listener that will be aware of whenever HTTP request data is sent over the wire
     */
    public interface DataSentListener {

        /**
         * Should be called whenever data is written to the HTTP request OutputStream
         *
         * @param bytesUploaded The number of bytes that were just written to the stream
         * @param totalBytes    The total number of bytes for the whole HTTP request
         */
        void onDataSent(long bytesUploaded, long totalBytes);

    }

}
