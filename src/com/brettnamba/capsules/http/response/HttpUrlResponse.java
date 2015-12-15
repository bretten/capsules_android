package com.brettnamba.capsules.http.response;

import com.brettnamba.capsules.http.HttpUrlConnectionRequest;

import org.apache.http.HttpStatus;

/**
 * HTTP response object that is used with HttpUrlConnectionRequest
 *
 * @author Brett Namba
 */
public class HttpUrlResponse {

    /**
     * The HTTP request object
     */
    protected HttpUrlConnectionRequest mRequest;

    /**
     * The HTTP response code
     */
    protected int mResponseCode;

    /**
     * Constructor
     *
     * @param request The HTTP request object
     */
    public HttpUrlResponse(HttpUrlConnectionRequest request) {
        this.mRequest = request;
        this.mResponseCode = this.mRequest.getResponseCode();
    }

    /**
     * Determines if this request was successful
     *
     * @return True if the request was successful
     */
    public boolean isSuccess() {
        return this.mResponseCode > HttpStatus.SC_OK &&
                this.mResponseCode < HttpStatus.SC_MULTIPLE_CHOICES;
    }

    /**
     * Determines if the request had a client error
     *
     * @return True if there was a client error
     */
    public boolean isClientError() {
        return this.mResponseCode >= HttpStatus.SC_BAD_REQUEST &&
                this.mResponseCode < HttpStatus.SC_INTERNAL_SERVER_ERROR;
    }

    /**
     * Determines if the request had a server error
     *
     * @return True if there was a server error
     */
    public boolean isServerError() {
        return this.mResponseCode >= HttpStatus.SC_INTERNAL_SERVER_ERROR &&
                this.mResponseCode < 600;
    }

    /**
     * Determines if the request had an error
     *
     * @return True if there was an error
     */
    public boolean isError() {
        return this.isClientError() || this.isServerError();
    }

}
