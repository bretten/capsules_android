package com.brettnamba.capsules.http.response;

import com.brettnamba.capsules.dataaccess.Capsule;
import com.brettnamba.capsules.util.JSONParser;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Models an HTTP response for opening a Capsule
 */
public class CapsuleOpenResponse extends ApiResponse {

    /**
     * The Capsule that was opened
     */
    private Capsule mCapsule;

    /**
     * Constructor that wraps an HttpResponse object
     *
     * Uses the super constructor to have it parse the response during construction
     *
     * @param httpResponse HttpResponse object
     * @throws IOException
     */
    public CapsuleOpenResponse(HttpResponse httpResponse) throws IOException {
        super(httpResponse);
    }

    /**
     * Gets the opened Capsule
     *
     * @return The opened Capsule
     */
    public Capsule getCapsule() {
        return this.mCapsule;
    }

    /**
     * Parses the opened Capsule and any messages from the JSON response body
     *
     * @param response The HTTP response object
     * @throws IOException
     */
    @Override
    protected void parse(HttpResponse response) throws IOException {
        if (response.getEntity() != null) {
            String body = EntityUtils.toString(response.getEntity());
            try {
                JSONObject jsonObject = new JSONObject(body);
                this.mCapsule = JSONParser.parseOpenCapsule(jsonObject);
                this.mMessages = JSONParser.parseMessages(jsonObject);
            } catch (JSONException e) {
                this.mCapsule = null;
                this.mMessages = new ArrayList<String>();
            }
        }
    }

}
