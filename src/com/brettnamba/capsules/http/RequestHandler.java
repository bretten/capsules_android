package com.brettnamba.capsules.http;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.util.Base64;

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
     * @return String
     * @throws ParseException 
     * @throws IOException
     */
    public String authenticate(String username, String password) throws ParseException, IOException {
        // GET
        HttpGet request = new HttpGet(RequestContract.BASE_URL + RequestContract.Uri.AUTH_URI);

        // Headers
        request.addHeader(HTTP.TARGET_HOST, RequestContract.HOST);
        request.addHeader(RequestContract.AUTH_HEADER, "Basic " + Base64.encodeToString((username + ":" + password).getBytes(), Base64.DEFAULT));

        // Send and get the response
        HttpResponse response = mClient.execute(request);
        return EntityUtils.toString(response.getEntity());
    }

    /**
     * Sends a request to the server to get all the undiscovered capsules in the area.
     * 
     * @param authToken
     * @param lat
     * @param lng
     * @return String
     * @throws ParseException 
     * @throws IOException
     */
    public String requestUndiscoveredCapsules(String authToken, double lat, double lng) throws ParseException, IOException {
        // POST
        HttpPost request = new HttpPost(RequestContract.BASE_URL + RequestContract.Uri.UNDISCOVERED_CAPSULES_URI);

        // Headers
        request.addHeader(HTTP.TARGET_HOST, RequestContract.HOST);
        request.addHeader(RequestContract.AUTH_HEADER, Base64.encodeToString((authToken).getBytes(), Base64.URL_SAFE|Base64.NO_WRAP));
        request.addHeader(HTTP.CONTENT_TYPE, URLEncodedUtils.CONTENT_TYPE);

        // POST body
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("data[" + RequestContract.Field.CAPSULE_LATITUDE + "]", Double.toString(lat)));
        params.add(new BasicNameValuePair("data[" + RequestContract.Field.CAPSULE_LONGITUDE + "]", Double.toString(lng)));
        request.setEntity(new UrlEncodedFormEntity(params));

        // Send and get the response
        HttpResponse response = mClient.execute(request);
        return EntityUtils.toString(response.getEntity());
    }
}
