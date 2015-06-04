package com.brettnamba.capsules.http;

import android.util.Base64;

import com.brettnamba.capsules.dataaccess.Capsule;
import com.brettnamba.capsules.dataaccess.CapsuleOwnership;

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
     * Authenticates a user via GET and returns their authentication token.
     *
     * @param username
     * @param password
     * @return HttpResponse
     * @throws ParseException
     * @throws IOException
     */
    public HttpResponse authenticate(String username, String password) throws IOException {
        // GET
        HttpGet request = new HttpGet(RequestContract.BASE_URL + RequestContract.Uri.AUTH_URI);

        // Headers
        request.addHeader(HTTP.TARGET_HOST, RequestContract.HOST);
        request.addHeader(RequestContract.AUTH_HEADER, "Basic " + Base64.encodeToString((username + ":" + password).getBytes(), Base64.DEFAULT));

        // Send and get the response
        return this.mClient.execute(request);
    }

    /**
     * Registers a user and returns an authentication token.
     *
     * @param username
     * @param email
     * @param password
     * @param passwordConfirm
     * @return
     * @throws IOException
     */
    public HttpResponse register(String username, String email, String password, String passwordConfirm) throws IOException {
        // POST
        HttpPost request = new HttpPost(RequestContract.BASE_URL + RequestContract.Uri.REGISTER_URI);

        // Headers
        request.addHeader(HTTP.TARGET_HOST, RequestContract.HOST);
        request.addHeader(HTTP.CONTENT_TYPE, URLEncodedUtils.CONTENT_TYPE);

        // POST body
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("data[" + RequestContract.Field.USERNAME + "]", username));
        params.add(new BasicNameValuePair("data[" + RequestContract.Field.EMAIL + "]", email));
        params.add(new BasicNameValuePair("data[" + RequestContract.Field.PASSWORD + "]", password));
        params.add(new BasicNameValuePair("data[" + RequestContract.Field.PASSWORD_CONFIRMATION + "]", passwordConfirm));
        request.setEntity(new UrlEncodedFormEntity(params));

        // Send and get the response
        return this.mClient.execute(request);
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

}
