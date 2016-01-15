package com.brettnamba.capsules.http.response;

import com.brettnamba.capsules.dataaccess.Capsule;
import com.brettnamba.capsules.http.HttpUrlConnectionRequest;
import com.brettnamba.capsules.util.JSONParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a JSON response object from a HttpUrlConnectionRequest
 *
 * @author Brett Namba
 */
public class JsonResponse extends HttpUrlResponse {

    /**
     * The JSON object
     */
    private JSONObject mJsonObject;

    /**
     * The authentication token that was parsed from the response
     */
    private String mAuthToken;

    /**
     * The collection tag that was parsed from the response
     */
    private String mCtag;

    /**
     * The messages that were parsed from the response
     */
    private List<String> mMessages;

    /**
     * The Capsules that were parsed from the response
     */
    private List<Capsule> mCapsules;

    /**
     * Constructor that parses the response body
     *
     * @param request The HTTP request object
     */
    public JsonResponse(HttpUrlConnectionRequest request) {
        super(request);
        // Parse the response body
        this.parseResponseBody(request);
    }

    /**
     * Parses the authentication token if it has not already has been parsed and then returns it
     *
     * @return The authentication token
     */
    public String getAuthToken() {
        if (this.mAuthToken != null) {
            return this.mAuthToken;
        }

        try {
            this.mAuthToken = JSONParser.parseAuthToken(this.mJsonObject);
        } catch (JSONException e) {
            this.mAuthToken = "";
        }

        return this.mAuthToken;
    }

    /**
     * Parses the collection tag if it has not already has been parsed and then returns it
     *
     * @return The collection tag
     */
    public String getCtag() {
        if (this.mCtag != null) {
            return this.mCtag;
        }

        try {
            this.mCtag = JSONParser.parseCtag(this.mJsonObject);
        } catch (JSONException e) {
            this.mCtag = "";
        }

        return this.mCtag;
    }

    /**
     * Parses the messages if they have not already been parsed and then returns them
     *
     * @return Collection of messages in the response
     */
    public List<String> getMessages() {
        if (this.mMessages != null) {
            return this.mMessages;
        }

        try {
            this.mMessages = JSONParser.parseMessages(this.mJsonObject);
        } catch (JSONException e) {
            this.mMessages = new ArrayList<String>();
        }

        return this.mMessages;
    }

    /**
     * Parses the Capsules if they have not already been parsed and then returns them
     *
     * @return Collection of Capsules from the response
     */
    public List<Capsule> getCapsules() {
        if (this.mCapsules != null) {
            return this.mCapsules;
        }

        try {
            this.mCapsules = JSONParser.parseCapsules(this.mJsonObject);
        } catch (JSONException e) {
            this.mCapsules = new ArrayList<Capsule>();
        }

        return this.mCapsules;
    }

    /**
     * Parses the HTTP response body
     *
     * @param request The executed HTTP request object with its response body
     */
    private void parseResponseBody(HttpUrlConnectionRequest request) {
        try {
            this.mJsonObject = new JSONObject(request.getResponseBody());
        } catch (NullPointerException | JSONException e) {
            this.mJsonObject = new JSONObject();
        }
    }

}
