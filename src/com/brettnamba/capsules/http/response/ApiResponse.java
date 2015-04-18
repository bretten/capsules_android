package com.brettnamba.capsules.http.response;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an HTTP response from the API
 */
public abstract class ApiResponse {

    /**
     * Reference to the HttpResponse object
     */
    protected HttpResponse mHttpResponse;

    /**
     * Messages parsed from the response
     */
    protected List<String> mMessages;

    /**
     * Default constructor
     */
    protected ApiResponse() {}

    /**
     * Constructor that instantiates an ApiResponse given an HttpResponse
     *
     * Parses the HttpResponse body to get the required data
     *
     * @param httpResponse The HttpResponse that will be parsed
     * @throws IOException
     * @throws JSONException
     */
    protected ApiResponse(HttpResponse httpResponse) throws IOException, JSONException {
        this.mHttpResponse = httpResponse;
        this.mMessages = new ArrayList<String>();
        this.parse(this.mHttpResponse);
    }

    /**
     * Gets the HttpResponse
     * @return HttpResponse Represents the HTTP response from the API
     */
    public HttpResponse getResponse() {
        return this.mHttpResponse;
    }

    /**
     * Gets the collection of messages parsed from the HTTP response
     *
     * @return Collection of messages to display to the user
     */
    public List<String> getMessages() {
        return this.mMessages;
    }

    /**
     * Determines if the response was a client error by checking if the status code is a
     * 4xx status code
     *
     * @return boolean True if it is a client error, false if it is not
     */
    public boolean isClientError() {
        if (this.mHttpResponse != null) {
            // If the status code is 4xx, then it is a client error
            if (this.mHttpResponse.getStatusLine().getStatusCode() >= HttpStatus.SC_BAD_REQUEST
                    && this.mHttpResponse.getStatusLine().getStatusCode() < HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if the response was a server error by checking if the status code is a 5xx
     * status code
     *
     * @return boolean True if it is a server error, false if it is not
     */
    public boolean isServerError() {
        if (this.mHttpResponse != null) {
            // If the status code is 5xx, then it is a server error
            if (this.mHttpResponse.getStatusLine().getStatusCode() >= HttpStatus.SC_INTERNAL_SERVER_ERROR
                    && this.mHttpResponse.getStatusLine().getStatusCode() < 600) {
                return true;
            }
        }
        return false;
    }

    /**
     * Should parse the body of the HttpResponse, set the messages, and any other data from the
     * HTTP response
     *
     * @param response The HTTP response object
     * @throws IOException
     * @throws JSONException
     */
    protected abstract void parse(HttpResponse response) throws IOException, JSONException;

}
