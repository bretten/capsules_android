package com.brettnamba.capsules.http.response;

import com.brettnamba.capsules.dataaccess.CapsulePojo;
import com.brettnamba.capsules.util.JSONParser;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * HTTP response object that models a response from the API containing undiscovered Capsules
 */
public class CapsulePingResponse extends ApiResponse {

    /**
     * Collection of undiscovered Capsules in the response
     */
    private List<CapsulePojo> mCapsules;

    /**
     * Constructor that wraps an HttpResponse object
     *
     * Uses the super constructor to have it parse the response during construction
     *
     * @param httpResponse HttpResponse object
     * @throws IOException
     */
    public CapsulePingResponse(HttpResponse httpResponse) throws IOException {
        super(httpResponse);
    }

    /**
     * Returns the collection of undiscovered Capsules
     *
     * @return Collection of undiscovered Capsules
     */
    public List<CapsulePojo> getCapsules() {
        return this.mCapsules;
    }

    /**
     * Parses the collection of undiscovered Capsules and any messages from the JSON response body
     *
     * @param response The HTTP response object
     * @throws java.io.IOException
     * @throws org.json.JSONException
     */
    @Override
    protected void parse(HttpResponse response) throws IOException {
        if (response.getEntity() != null) {
            String body = EntityUtils.toString(response.getEntity());
            try {
                JSONObject jsonObject = new JSONObject(body);
                this.mCapsules = JSONParser.parseUndiscoveredCapsules(jsonObject);
                this.mMessages = JSONParser.parseMessages(jsonObject);
            } catch (JSONException e) {
                this.mCapsules = new ArrayList<CapsulePojo>();
                this.mMessages = new ArrayList<String>();
            }
        }
    }

}
