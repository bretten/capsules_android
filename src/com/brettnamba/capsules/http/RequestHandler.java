package com.brettnamba.capsules.http;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Base64;

import com.brettnamba.capsules.Constants;

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
     * @throws ClientProtocolException
     * @throws IOException
     * @throws JSONException
     */
    public String authenticate(String username, String password) throws ClientProtocolException, IOException, JSONException {
        // GET
        HttpGet request = new HttpGet(Constants.BASE_URL + Constants.AUTH_URI);

        // Headers
        request.addHeader(HTTP.TARGET_HOST, Constants.HOST);
        request.addHeader(Constants.AUTH_HEADER, "Basic " + Base64.encodeToString((username + ":" + password).getBytes(), Base64.DEFAULT));

        // Send and get the response
        HttpResponse response = mClient.execute(request);
        String body = EntityUtils.toString(response.getEntity());

        // Parse and get the token
        JSONObject json = new JSONObject(body);

        return json.getString(Constants.AUTH_TOKEN_RESPONSE_FIELD);
    }

    /**
     * Sends a request to the server to get all the undiscovered capsules in the area.
     * 
     * @param authToken
     * @param lat
     * @param lng
     * @return JSONArray
     * @throws ClientProtocolException
     * @throws IOException
     * @throws JSONException
     */
    public JSONArray requestUndiscoveredCapsules(String authToken, double lat, double lng)
            throws ClientProtocolException, IOException, JSONException {
        // POST
        HttpPost request = new HttpPost(Constants.BASE_URL + Constants.UNDISCOVERED_CAPSULES_URI);

        // Headers
        request.addHeader(HTTP.TARGET_HOST, Constants.HOST);
        request.addHeader(Constants.AUTH_HEADER, Base64.encodeToString((authToken).getBytes(), Base64.URL_SAFE|Base64.NO_WRAP));
        request.addHeader(HTTP.CONTENT_TYPE, URLEncodedUtils.CONTENT_TYPE);

        // POST body
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("data[lat]", Double.toString(lat)));
        params.add(new BasicNameValuePair("data[lng]", Double.toString(lng)));
        request.setEntity(new UrlEncodedFormEntity(params));

        // Send and get the response
        HttpResponse response = mClient.execute(request);
        String body = EntityUtils.toString(response.getEntity());

        // Parse and return the capsules
        return new JSONArray(body);
        
    }
}
