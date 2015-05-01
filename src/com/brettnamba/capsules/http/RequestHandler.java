package com.brettnamba.capsules.http;

import android.util.Base64;

import com.brettnamba.capsules.dataaccess.Capsule;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles general HTTP requests and responses.
 * 
 * @author Brett Namba
 *
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
        return mClient.execute(request);
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
        return mClient.execute(request);
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
        return mClient.execute(request);
    }

    /**
     * Sends a request to the server to "open" a Capsule.
     * 
     * @param authToken
     * @param syncId
     * @param lat
     * @param lng
     * @return
     * @throws ParseException
     * @throws IOException
     */
    public String requestOpenCapsule(String authToken, long syncId, double lat, double lng) throws ParseException, IOException {
         // POST
        HttpPost request = new HttpPost(RequestContract.BASE_URL + RequestContract.Uri.OPEN_CAPSULE_URI);
        
        // Headers
        request.addHeader(HTTP.TARGET_HOST, RequestContract.HOST);
        request.addHeader(RequestContract.AUTH_HEADER, Base64.encodeToString((authToken).getBytes(), Base64.URL_SAFE|Base64.NO_WRAP));
        request.addHeader(HTTP.CONTENT_TYPE, URLEncodedUtils.CONTENT_TYPE);
        
        // POST body
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("data[" + RequestContract.Field.CAPSULE_SYNC_ID + "]", Long.toString(syncId)));
        params.add(new BasicNameValuePair("data[" + RequestContract.Field.CAPSULE_LATITUDE + "]", Double.toString(lat)));
        params.add(new BasicNameValuePair("data[" + RequestContract.Field.CAPSULE_LONGITUDE + "]", Double.toString(lng)));
        request.setEntity(new UrlEncodedFormEntity(params));
        
        // Send and get the response
        HttpResponse response = mClient.execute(request);
        return EntityUtils.toString(response.getEntity());
    }

    /**
     * Sends a request to the server to retrieve the ctag for the collection specified in the URI.
     * 
     * @param authToken
     * @param uri
     * @return
     * @throws ParseException
     * @throws IOException
     */
    public String requestCtag(String authToken, String uri) throws ParseException, IOException {
        // GET
        HttpGet request = new HttpGet(RequestContract.BASE_URL + uri);

        // Headers
        request.addHeader(HTTP.TARGET_HOST, RequestContract.HOST);
        request.addHeader(RequestContract.AUTH_HEADER, Base64.encodeToString((authToken).getBytes(), Base64.URL_SAFE|Base64.NO_WRAP));

        // Send and get the response
        HttpResponse response = mClient.execute(request);
        return EntityUtils.toString(response.getEntity());
    }

    /**
     * Sends a request to the server to update an Ownership Capsule.
     * 
     * @param authToken
     * @param capsule
     * @return
     * @throws ParseException
     * @throws IOException
     */
    public String requestOwnershipUpdate(String authToken, Capsule capsule) throws ParseException, IOException {
        // POST
        HttpPost request = new HttpPost(RequestContract.BASE_URL + RequestContract.Uri.OWNERSHIP_URI);

        // Headers
        request.addHeader(HTTP.TARGET_HOST, RequestContract.HOST);
        request.addHeader(RequestContract.AUTH_HEADER, Base64.encodeToString((authToken).getBytes(), Base64.URL_SAFE|Base64.NO_WRAP));
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
        HttpResponse response = mClient.execute(request);
        return EntityUtils.toString(response.getEntity());
    }

    /**
     * Sends a DELETE request to the Capsule Ownership URI
     * 
     * @param authToken
     * @param capsuleId
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    public int requestOwnershipDelete(String authToken, long capsuleId) throws ClientProtocolException, IOException {
         // DELETE
        HttpDelete request = new HttpDelete(RequestContract.BASE_URL + RequestContract.Uri.OWNERSHIP_URI + "/" + String.valueOf(capsuleId));

        // Headers
        request.addHeader(HTTP.TARGET_HOST, RequestContract.HOST);
        request.addHeader(RequestContract.AUTH_HEADER, Base64.encodeToString((authToken).getBytes(), Base64.URL_SAFE|Base64.NO_WRAP));

        // Send and get the response
        HttpResponse response = mClient.execute(request);
        return response.getStatusLine().getStatusCode();
    }

    /**
     * Requests the Capsule Ownership status information
     * 
     * @param authToken
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    public String requestOwnershipStatus(String authToken) throws ClientProtocolException, IOException {
        // GET
        HttpGet request = new HttpGet(RequestContract.BASE_URL + RequestContract.Uri.OWNERSHIP_STATUS_URI);

        // Headers
        request.addHeader(HTTP.TARGET_HOST, RequestContract.HOST);
        request.addHeader(RequestContract.AUTH_HEADER, Base64.encodeToString((authToken).getBytes(), Base64.URL_SAFE|Base64.NO_WRAP));

        // Send and get the response
        HttpResponse response = mClient.execute(request);
        return EntityUtils.toString(response.getEntity());
    }

    /**
     * Requests a REPORT on the given Capsule Ownerships
     * 
     * @param authToken
     * @param capsules
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    public String requestOwnershipReport(String authToken, List<Capsule> capsules) throws ClientProtocolException, IOException {
        // POST
        HttpPost request = new HttpPost(RequestContract.BASE_URL + RequestContract.Uri.OWNERSHIP_REPORT_URI);

        // Headers
        request.addHeader(HTTP.TARGET_HOST, RequestContract.HOST);
        request.addHeader(RequestContract.AUTH_HEADER, Base64.encodeToString((authToken).getBytes(), Base64.URL_SAFE|Base64.NO_WRAP));
        request.addHeader(HTTP.CONTENT_TYPE, URLEncodedUtils.CONTENT_TYPE);

        // POST body
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        for (int i = 0; i < capsules.size(); i++) {
            params.add(new BasicNameValuePair("data[" + RequestContract.Field.CAPSULE_SYNC_ID + "][" + i + "]", Long.toString(capsules.get(i).getSyncId())));
        }
        request.setEntity(new UrlEncodedFormEntity(params));

        // Send and get the response
        HttpResponse response = mClient.execute(request);
        return EntityUtils.toString(response.getEntity());
    }

}
