package com.brettnamba.capsules.http;

import android.accounts.Account;
import android.content.Context;
import android.util.Base64;

import com.brettnamba.capsules.Constants;
import com.brettnamba.capsules.dataaccess.Capsule;
import com.brettnamba.capsules.dataaccess.CapsuleOwnership;
import com.brettnamba.capsules.dataaccess.Memoir;
import com.brettnamba.capsules.http.response.JsonResponse;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles general HTTP requests and responses.
 *
 * @author Brett Namba
 */
public class RequestHandler {

    /**
     * The HttpClient that executes the requests and responses.
     */
    private HttpClient mClient;

    /**
     * The logging tag.
     */
    private static final String TAG = "RequestHandler";

    /**
     * Constructor
     *
     * @param client
     */
    public RequestHandler(HttpClient client) {
        this.mClient = client;
    }

    /**
     * Sends an HTTP request to authenticate a user with the specified username and password
     *
     * @param context  The current Context
     * @param username The username
     * @param password The password
     * @return HTTP response object
     * @throws IOException
     */
    public static JsonResponse authenticate(Context context, String username, String password)
            throws IOException {
        // Initialize the request
        HttpUrlGetRequest request = new HttpUrlGetRequest(context,
                RequestContract.BASE_URL + RequestContract.Uri.AUTH_URI);
        request.addRequestHeader("Accept", "application/json");

        // Add the authentication header
        request.addRequestHeader(RequestContract.AUTH_HEADER, "Basic " +
                Base64.encodeToString((username + ":" + password).getBytes(), Base64.DEFAULT));

        // Send
        request.send();

        // Initialize the response
        return new JsonResponse(request);
    }

    /**
     * Sends an HTTP request that registers a new user
     *
     * @param context         The current Context
     * @param username        The username to register
     * @param email           The email
     * @param password        The password
     * @param passwordConfirm The password confirmation
     * @return HTTP response object
     * @throws IOException
     */
    public static JsonResponse register(Context context, String username, String email,
                                        String password, String passwordConfirm)
            throws IOException {
        // Initialize the request
        HttpUrlWwwFormRequest request = new HttpUrlWwwFormRequest(context,
                RequestContract.BASE_URL + RequestContract.Uri.REGISTER_URI);
        request.addRequestHeader("Accept", "application/json");

        // Add the request body
        request.addRequestParameter(RequestContract.Field.USERNAME, username);
        request.addRequestParameter(RequestContract.Field.EMAIL, email);
        request.addRequestParameter(RequestContract.Field.PASSWORD, password);
        request.addRequestParameter(RequestContract.Field.PASSWORD_CONFIRMATION, passwordConfirm);

        // Send
        request.send();

        // Initialize the response
        return new JsonResponse(request);
    }

    /**
     * Sends a HTTP request to the API to get undiscovered Capsules for a user at a given
     * location
     *
     * @param authToken The authentication token for the user
     * @param lat       The user's latitude
     * @param lng       The user's longitude
     * @return HTTP response object containing undiscovered Capsules
     * @throws ParseException
     * @throws IOException
     */
    public HttpResponse requestUndiscoveredCapsules(String authToken, String lat, String lng) throws ParseException, IOException {
        // POST
        HttpPost request = new HttpPost(RequestContract.BASE_URL + RequestContract.Uri.UNDISCOVERED_CAPSULES_URI);

        // Headers
        request.addHeader(HTTP.TARGET_HOST, RequestContract.HOST);
        request.addHeader(RequestContract.AUTH_HEADER, Base64.encodeToString((authToken).getBytes(), Base64.URL_SAFE | Base64.NO_WRAP));
        request.addHeader(HTTP.CONTENT_TYPE, URLEncodedUtils.CONTENT_TYPE);

        // POST body
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("data[" + RequestContract.Field.CAPSULE_LATITUDE + "]", lat));
        params.add(new BasicNameValuePair("data[" + RequestContract.Field.CAPSULE_LONGITUDE + "]", lng));
        request.setEntity(new UrlEncodedFormEntity(params));

        // Send the request and get the response
        return this.mClient.execute(request);
    }

    /**
     * Sends a request to the API to open an undiscovered Capsule
     *
     * @param authToken The user's authentication token
     * @param syncId    The sync ID of the Capsule being opened
     * @param lat       The user's latitude to verify their position
     * @param lng       The user's longitude to verify their position
     * @return HTTP response object containing the Capsule on success
     * @throws ParseException
     * @throws IOException
     */
    public HttpResponse requestOpenCapsule(String authToken, String syncId, String lat, String lng) throws ParseException, IOException {
        // POST
        HttpPost request = new HttpPost(RequestContract.BASE_URL + RequestContract.Uri.OPEN_CAPSULE_URI);

        // Headers
        request.addHeader(HTTP.TARGET_HOST, RequestContract.HOST);
        request.addHeader(RequestContract.AUTH_HEADER, Base64.encodeToString((authToken).getBytes(), Base64.URL_SAFE | Base64.NO_WRAP));
        request.addHeader(HTTP.CONTENT_TYPE, URLEncodedUtils.CONTENT_TYPE);

        // POST body
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("data[" + RequestContract.Field.CAPSULE_SYNC_ID + "]", syncId));
        params.add(new BasicNameValuePair("data[" + RequestContract.Field.CAPSULE_LATITUDE + "]", lat));
        params.add(new BasicNameValuePair("data[" + RequestContract.Field.CAPSULE_LONGITUDE + "]", lng));
        request.setEntity(new UrlEncodedFormEntity(params));

        // Send and get the response
        return this.mClient.execute(request);
    }

    /**
     * Sends a request to the server to retrieve the ctag for the collection specified in the URI
     *
     * @param authToken The authentication token
     * @param uri       The URI that determines which collection to get the ctag for
     * @return HTTP response object
     * @throws ParseException
     * @throws IOException
     */
    public HttpResponse requestCtag(String authToken, String uri) throws ParseException, IOException {
        // GET
        HttpGet request = new HttpGet(RequestContract.BASE_URL + uri);

        // Headers
        request.addHeader(HTTP.TARGET_HOST, RequestContract.HOST);
        request.addHeader(RequestContract.AUTH_HEADER, Base64.encodeToString((authToken).getBytes(), Base64.URL_SAFE | Base64.NO_WRAP));

        // Send and get the response
        return this.mClient.execute(request);
    }

    /**
     * Sends a request to the server to update an Ownership Capsule
     *
     * @param authToken The authentication token
     * @param capsule   The Capsule to update
     * @return HttpResponse object
     * @throws ParseException
     * @throws IOException
     */
    public HttpResponse requestOwnershipUpdate(String authToken, Capsule capsule) throws ParseException, IOException {
        // POST
        HttpPost request = new HttpPost(RequestContract.BASE_URL + RequestContract.Uri.OWNERSHIP_URI + "/" + String.valueOf(capsule.getSyncId()));

        // Headers
        request.addHeader(HTTP.TARGET_HOST, RequestContract.HOST);
        request.addHeader(RequestContract.AUTH_HEADER, Base64.encodeToString((authToken).getBytes(), Base64.URL_SAFE | Base64.NO_WRAP));
        request.addHeader(HTTP.CONTENT_TYPE, URLEncodedUtils.CONTENT_TYPE);

        // POST body
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        if (capsule.getSyncId() > 0) {
            params.add(new BasicNameValuePair("data[" + RequestContract.Field.CAPSULE_SYNC_ID + "]", Long.toString(capsule.getSyncId())));
        }
        params.add(new BasicNameValuePair("data[" + RequestContract.Field.CAPSULE_NAME + "]", capsule.getName()));
        params.add(new BasicNameValuePair("data[" + RequestContract.Field.CAPSULE_LATITUDE + "]", Double.toString(capsule.getLatitude())));
        params.add(new BasicNameValuePair("data[" + RequestContract.Field.CAPSULE_LONGITUDE + "]", Double.toString(capsule.getLongitude())));
        request.setEntity(new UrlEncodedFormEntity(params));

        // Send and get the response
        return this.mClient.execute(request);
    }

    /**
     * Sends a DELETE request for the specified Capsule
     *
     * @param authToken The Capsule to DELETE
     * @param syncId    The sync ID of the Capsule
     * @return HTTP response object
     * @throws IOException
     */
    public HttpResponse requestOwnershipDelete(String authToken, long syncId) throws IOException {
        // DELETE
        HttpDelete request = new HttpDelete(RequestContract.BASE_URL + RequestContract.Uri.OWNERSHIP_URI + "/" + String.valueOf(syncId));

        // Headers
        request.addHeader(HTTP.TARGET_HOST, RequestContract.HOST);
        request.addHeader(RequestContract.AUTH_HEADER, Base64.encodeToString((authToken).getBytes(), Base64.URL_SAFE | Base64.NO_WRAP));

        // Send and get the response
        return this.mClient.execute(request);
    }

    /**
     * Returns the status of a user's Capsules
     *
     * @param authToken The authentication token
     * @return Collection of user's Capsules and their status
     * @throws IOException
     */
    public HttpResponse requestOwnershipStatus(String authToken) throws IOException {
        // GET
        HttpGet request = new HttpGet(RequestContract.BASE_URL + RequestContract.Uri.OWNERSHIP_STATUS_URI);

        // Headers
        request.addHeader(HTTP.TARGET_HOST, RequestContract.HOST);
        request.addHeader(RequestContract.AUTH_HEADER, Base64.encodeToString((authToken).getBytes(), Base64.URL_SAFE | Base64.NO_WRAP));

        // Send and get the response
        return this.mClient.execute(request);
    }

    /**
     * Requests a report on the specified Capsules
     *
     * @param authToken The authentication token
     * @param capsules  The Capsules to report on
     * @return HTTP response object
     * @throws IOException
     */
    public HttpResponse requestOwnershipReport(String authToken, List<CapsuleOwnership> capsules) throws IOException {
        // POST
        HttpPost request = new HttpPost(RequestContract.BASE_URL + RequestContract.Uri.OWNERSHIP_REPORT_URI);

        // Headers
        request.addHeader(HTTP.TARGET_HOST, RequestContract.HOST);
        request.addHeader(RequestContract.AUTH_HEADER, Base64.encodeToString((authToken).getBytes(), Base64.URL_SAFE | Base64.NO_WRAP));
        request.addHeader(HTTP.CONTENT_TYPE, URLEncodedUtils.CONTENT_TYPE);

        // POST body
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        for (int i = 0; i < capsules.size(); i++) {
            params.add(new BasicNameValuePair("data[" + RequestContract.Field.CAPSULE_SYNC_ID + "][" + i + "]", Long.toString(capsules.get(i).getSyncId())));
        }
        request.setEntity(new UrlEncodedFormEntity(params));

        // Send and get the response
        return this.mClient.execute(request);
    }

    /**
     * Requests a collection of Capsules
     *
     * @param context    The current Context
     * @param account    The Account to be used for authentication
     * @param parameters Parameters for the HTTP request
     * @return HTTP response object
     */
    public static JsonResponse requestCapsules(Context context, Account account,
                                               CapsuleRequestParameters parameters) {
        // Initialize the request
        HttpUrlGetRequest request = new HttpUrlGetRequest(context,
                RequestContract.BASE_URL + RequestContract.Uri.CAPSULES_URI, account,
                Constants.AUTH_TOKEN_TYPE);
        request.addRequestHeader("Accept", "application/json");

        // Add the query parameters
        request.addAllQueryParameters(parameters.getAsCollection());

        // Send the request
        request.send();

        return new JsonResponse(request);
    }

    /**
     * Requests a collection of Discoveries
     *
     * @param context    The current Context
     * @param account    The Account to be used for authentication
     * @param parameters Parameters for the HTTP request
     * @return HTTP response object
     */
    public static JsonResponse requestDiscoveries(Context context, Account account,
                                                  CapsuleRequestParameters parameters) {
        // Initialize the request
        HttpUrlGetRequest request = new HttpUrlGetRequest(context,
                RequestContract.BASE_URL + RequestContract.Uri.DISCOVERIES_URI, account,
                Constants.AUTH_TOKEN_TYPE);
        request.addRequestHeader("Accept", "application/json");

        // Add the query parameters
        request.addAllQueryParameters(parameters.getAsCollection());

        // Send the request
        request.send();

        return new JsonResponse(request);
    }

    /**
     * Sends a Capsule validation request and returns HTTP request object also containing the
     * request data
     *
     * @param context            The current Context
     * @param account            The Account used to authenticate the request
     * @param capsule            The Capsule to validate
     * @param onDataSentListener The listener that will receive updates on the amount of data sent.
     *                           If not needed, use null.
     * @return The HTTP request object which also contains the response data
     */
    public static HttpUrlWwwFormRequest validateCapsule(Context context, Account account,
                                                        Capsule capsule,
                                                        HttpUrlConnectionRequest.DataSentListener
                                                                onDataSentListener) {
        HttpUrlWwwFormRequest httpRequest = new HttpUrlWwwFormRequest(
                context,
                RequestContract.BASE_URL + RequestContract.Uri.VALIDATE_CAPSULE_URI,
                account, Constants.AUTH_TOKEN_TYPE
        );
        // Add the listener
        if (onDataSentListener != null) {
            httpRequest.setListener(onDataSentListener);
        }
        // Add the request parameters
        RequestHandler.addCapsuleRequestParameters(httpRequest, capsule);
        // Send the request
        httpRequest.send();

        return httpRequest;
    }

    /**
     * Sends a Capsule save request and returns the HTTP request object also containing the
     * response data
     *
     * @param context            The current Context
     * @param account            The Account used to authenticate the request
     * @param capsule            The Capsule to save
     * @param onDataSentListener The listener that will receive updates on the amount of data sent.
     *                           If not needed, use null.
     * @return The HTTP request object which also contains the response data
     */
    public static HttpUrlMultiPartRequest saveCapsule(Context context, Account account,
                                                      Capsule capsule,
                                                      HttpUrlConnectionRequest.DataSentListener
                                                              onDataSentListener) {
        HttpUrlMultiPartRequest httpRequest = new HttpUrlMultiPartRequest(
                context,
                RequestContract.BASE_URL + RequestContract.Uri.SAVE_CAPSULE_URI,
                account, Constants.AUTH_TOKEN_TYPE
        );
        // Add the listener
        if (onDataSentListener != null) {
            httpRequest.setListener(onDataSentListener);
        }
        // Add the request parameters
        RequestHandler.addCapsuleRequestParameters(httpRequest, capsule);
        // Add the file upload to the request
        RequestHandler.addCapsuleUploadRequestParameters(httpRequest, capsule);
        // Execute
        httpRequest.send();

        return httpRequest;
    }

    /**
     * Opens a connection to a Memoir resource and returns the HTTP request object
     *
     * @param context  The current Context
     * @param account  The Account used to authenticate the request
     * @param memoirId The server-side ID of the Memoir
     * @return The HTTP request object
     */
    public static HttpUrlGetRequest requestMemoirImage(Context context, Account account,
                                                       long memoirId) {
        // Initialize the request
        HttpUrlGetRequest request = new HttpUrlGetRequest(context,
                RequestContract.BASE_URL + RequestContract.Uri.MEMOIR_URI +
                        String.valueOf(memoirId), account, Constants.AUTH_TOKEN_TYPE);

        // Send the request
        request.connect();

        return request;
    }

    /**
     * Adds the standard Capsule request parameters to an instance of HttpUrlConnectionRequest
     *
     * @param httpRequest The HttpUrlConnectionRequest to add the request parameters to
     * @param capsule     The Capsule to get the request parameter data from
     */
    public static void addCapsuleRequestParameters(HttpUrlConnectionRequest httpRequest,
                                                   Capsule capsule) {
        if (capsule.getName() != null) {
            httpRequest.addRequestParameter(
                    String.format("data[%1$s][%2$s]", RequestContract.Field.CAPSULE_ENTITY,
                            RequestContract.Field.CAPSULE_NAME), capsule.getName());
        }
        httpRequest.addRequestParameter(
                String.format("data[%1$s][%2$s]", RequestContract.Field.CAPSULE_ENTITY,
                        RequestContract.Field.CAPSULE_LATITUDE),
                String.valueOf(capsule.getLatitude()));
        httpRequest.addRequestParameter(
                String.format("data[%1$s][%2$s]", RequestContract.Field.CAPSULE_ENTITY,
                        RequestContract.Field.CAPSULE_LONGITUDE),
                String.valueOf(capsule.getLongitude()));
        Memoir memoir = capsule.getMemoir();
        if (memoir != null) {
            if (memoir.getTitle() != null) {
                httpRequest.addRequestParameter(
                        String.format("data[%1$s][0][%2$s]", RequestContract.Field.MEMOIR_ENTITY,
                                RequestContract.Field.MEMOIR_TITLE), memoir.getTitle());
            }
            if (memoir.getMessage() != null) {
                httpRequest.addRequestParameter(
                        String.format("data[%1$s][0][%2$s]", RequestContract.Field.MEMOIR_ENTITY,
                                RequestContract.Field.MEMOIR_MESSAGE), memoir.getMessage());
            }
        }
    }

    /**
     * Adds the Capsule file upload request parameters to an instance of HttpUrlMultiPartRequest
     *
     * @param httpRequest The HttpUrlMultiPartRequest to add the file upload request parameters to
     * @param capsule     The Capsule to get the file upload request parameter data from
     */
    public static void addCapsuleUploadRequestParameters(HttpUrlMultiPartRequest httpRequest,
                                                         Capsule capsule) {
        if (capsule != null && capsule.getMemoir() != null) {
            Memoir memoir = capsule.getMemoir();
            if (memoir.getFileContentUri() != null) {
                httpRequest.addFileUploadContentUri(
                        String.format("data[%1$s][0][%2$s]", RequestContract.Field.MEMOIR_ENTITY,
                                RequestContract.Field.MEMOIR_FILE), memoir.getFileContentUri());
            }
        }
    }

}
