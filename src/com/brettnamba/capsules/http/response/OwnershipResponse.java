package com.brettnamba.capsules.http.response;

import com.brettnamba.capsules.dataaccess.CapsuleOwnership;
import com.brettnamba.capsules.util.JSONParser;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Models an HTTP response that contains a single Capsule ownership
 */
public class OwnershipResponse extends ApiResponse {

    /**
     * Whether or not the request was a success
     */
    private boolean mSuccess;

    /**
     * The Capsule that was updated and sent back in the response body
     */
    private CapsuleOwnership mCapsule;

    /**
     * Constructor that wraps an HTTP response object and parses the response
     *
     * @param response HTTP response object
     * @throws IOException
     */
    public OwnershipResponse(HttpResponse response) throws IOException {
        super(response);
    }

    /**
     * Determines if the request was a success
     *
     * @return True if it is a success, false if it is not
     */
    public boolean isSuccess() {
        return this.mSuccess;
    }

    /**
     * Gets the Capsule that was sent in the response body
     *
     * @return The Capsule that was sent back in the response
     */
    public CapsuleOwnership getCapsule() {
        return this.mCapsule;
    }

    /**
     * Should parse the body of the HttpResponse, set the messages, and any other data from the
     * HTTP response
     *
     * @param response The HTTP response object
     * @throws IOException
     */
    @Override
    protected void parse(HttpResponse response) throws IOException {
        if (response.getStatusLine() != null) {
            this.mSuccess = response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
        }
        if (response.getEntity() != null) {
            String body = EntityUtils.toString(response.getEntity());
            try {
                JSONObject jsonObject = new JSONObject(body);
                this.mCapsule = JSONParser.parseOwnershipCapsule(jsonObject);
                this.mMessages = JSONParser.parseMessages(jsonObject);
            } catch (JSONException e) {
                this.mCapsule = null;
                this.mMessages = new ArrayList<String>();
            }
        }
    }

}
