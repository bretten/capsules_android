package com.brettnamba.capsules.http;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
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

        // HTTP host
        // TODO Remove hard coding
        request.addHeader("Host", Constants.HOST);

        // Authentication header
        // TODO Remove hard coding
        request.addHeader(Constants.AUTH_HEADER, "Basic " + Base64.encodeToString((username + ":" + password).getBytes(), Base64.DEFAULT));

        // Send and get the response
        HttpResponse response = mClient.execute(request);
        String body = EntityUtils.toString(response.getEntity());

        // Parse and get the token
        JSONObject json = new JSONObject(body);

        return json.getString(Constants.AUTH_TOKEN_RESPONSE_FIELD);
    }
}
