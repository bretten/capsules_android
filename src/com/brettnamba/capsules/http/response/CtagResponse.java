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
 * Models an HTTP response that contains a ctag
 */
public class CtagResponse extends ApiResponse {

    /**
     * Whether or not the request was a success
     */
    private boolean mSuccess;

    /**
     * The ctag from the response
     */
    private String mCtag;

    /**
     * Constructor that parses an HTTP response object
     *
     * @param response The HTTP response
     * @throws IOException
     */
    public CtagResponse(HttpResponse response) throws IOException {
        super(response);
    }

    /**
     * Determines if the request was a success
     *
     * @return True if it was a success, otherwise false
     */
    public boolean isSuccess() {
        return this.mSuccess;
    }

    /**
     * Gets the ctag
     *
     * @return The ctag
     */
    public String getCtag() {
        return this.mCtag;
    }

    /**
     * Gets the response and parses the JSON body
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
                this.mCtag = JSONParser.parseCtag(jsonObject);
                this.mMessages = JSONParser.parseMessages(jsonObject);
            } catch (JSONException e) {
                this.mCtag = null;
                this.mMessages = new ArrayList<String>();
            }
        }
    }

}
