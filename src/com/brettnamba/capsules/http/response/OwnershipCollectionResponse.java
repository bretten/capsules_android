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
import java.util.List;

/**
 * Models an HTTP response that contains a collection of Capsule ownerships
 */
public class OwnershipCollectionResponse extends ApiResponse {

    /**
     * Whether or not the request was a success
     */
    private boolean mSuccess;

    /**
     * The Capsules returned in the response body
     */
    private List<CapsuleOwnership> mCapsules;

    /**
     * Constructor that wraps an HTTP response object and parses out response body
     *
     * @param response HTTP response object
     * @throws IOException
     */
    public OwnershipCollectionResponse(HttpResponse response) throws IOException {
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
     * Gets the Capsules from the response body
     *
     * @return Capsules from the response body
     */
    public List<CapsuleOwnership> getCapsules() {
        return this.mCapsules;
    }

    /**
     * Parses the HTTP response body for the Capsules
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
                this.mCapsules = JSONParser.parseOwnershipCollection(jsonObject);
                this.mMessages = JSONParser.parseMessages(jsonObject);
            } catch (JSONException e) {
                this.mCapsules = new ArrayList<CapsuleOwnership>();
                this.mMessages = new ArrayList<String>();
            }
        }
    }

}
