package com.brettnamba.capsules.http.response;

import com.brettnamba.capsules.util.JSONParser;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Models an HTTP response for sending a DELETE request for an entity on the server
 */
public class EntityDeleteResponse extends ApiResponse {

    /**
     * Whether or not the DELETE was a success
     */
    private boolean mSuccess;

    /**
     * Constructor that wraps an HTTP response object and parses it
     *
     * @param response HTTP response object
     * @throws IOException
     */
    public EntityDeleteResponse(HttpResponse response) throws IOException {
        super(response);
    }

    /**
     * Determines if the DELETE was a success
     *
     * @return True if it is a success, false if it is not
     */
    public boolean isSuccess() {
        return this.mSuccess;
    }

    /**
     * Parses the HTTP response and checks if the status code was "No content" which indicates a
     * successful DELETE
     *
     * @param response The HTTP response object
     * @throws IOException
     */
    @Override
    protected void parse(HttpResponse response) throws IOException {
        if (response.getStatusLine() != null) {
            this.mSuccess = response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT;
        }
        if (response.getEntity() != null) {
            String body = EntityUtils.toString(response.getEntity());
            try {
                JSONObject jsonObject = new JSONObject(body);
                this.mMessages = JSONParser.parseMessages(jsonObject);
            } catch (JSONException e) {
                this.mMessages = new ArrayList<String>();
            }
        }
    }

}
