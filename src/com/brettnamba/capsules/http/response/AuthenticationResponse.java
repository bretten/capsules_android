package com.brettnamba.capsules.http.response;

import com.brettnamba.capsules.util.JSONParser;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Represents an authentication HTTP response from the API
 */
public class AuthenticationResponse extends ApiResponse {

    /**
     * The authentication token parsed from the response body
     */
    private String mAuthToken;

    /**
     * Builds an AuthenticationResponse given an HTTP response object
     *
     * @param httpResponse Represents the HTTP response
     * @throws IOException
     * @throws JSONException
     */
    public AuthenticationResponse(HttpResponse httpResponse) throws IOException, JSONException {
        super(httpResponse);
    }

    /**
     * Gets the authentication token
     *
     * @return String The authentication token
     */
    public String getAuthToken() {
        return this.mAuthToken;
    }

    /**
     * Gets the JSON body from the response and parses out the authentication token and
     * any messages
     *
     * @param response The HTTP response object
     * @throws IOException
     * @throws JSONException
     */
    @Override
    protected void parse(HttpResponse response) throws IOException, JSONException {
        if (response.getEntity() != null) {
            String body = EntityUtils.toString(response.getEntity());
            JSONObject jsonObject = new JSONObject(body);
            this.mAuthToken = JSONParser.parseAuthToken(jsonObject);
            this.mMessages = JSONParser.parseMessages(jsonObject);
        }
    }

}
