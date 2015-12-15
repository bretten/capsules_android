package com.brettnamba.capsules.http;

import android.accounts.Account;
import android.content.Context;
import android.net.Uri;
import android.support.v4.util.Pair;

import com.brettnamba.capsules.util.Files;

import org.apache.http.client.methods.HttpPost;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of HttpUrlConnectionRequest that provides functionality for sending
 * multi-part HTTP requests.
 *
 * @author Brett Namba
 */
public class HttpUrlMultiPartRequest extends HttpUrlConnectionRequest {

    /**
     * The String that represents the boundary in the multi-part request
     */
    private String mBoundary;

    /**
     * The boundary String as bytes
     */
    private byte[] mBoundaryBytes;

    /**
     * The final boundary String as bytes, where the final boundary is represented as the boundary
     * followed by two hyphens
     */
    private byte[] mFinalBoundaryBytes;

    /**
     * Collection bytes of all the request request parameters preceded by boundaries
     */
    private List<byte[]> mRequestParameterBytesCollection;

    /**
     * Collection mapping the file upload parameter name to the FileProvider content URI
     */
    private Map<String, Uri> mFileUploadContentUris;

    /**
     * Collection mapping the FileProvider content URI to the boundary + content information
     * of the file as bytes
     */
    private Map<Uri, byte[]> mFileUploadHeaderBytesCollection;

    /**
     * The total byte count that the file uploads will occupy in the HTTP request body
     */
    private long mTotalFileUploadByteCount = 0;

    /**
     * Newline String
     */
    private static final String NEW_LINE = "\r\n";

    /**
     * String of two hyphens that are meant to precede the multi-part boundaries and follow
     * the final boundary
     */
    private static final String TWO_HYPHENS = "--";

    /**
     * US-ASCII charset for getting Strings as bytes
     */
    private static final String US_ASCII = "US-ASCII";

    /**
     * UTF-8 charset for getting Strings as bytes
     */
    private static final String UTF_8 = "UTF-8";

    /**
     * The buffer length when writing file uploads to the HTTP request stream in parts
     */
    private static final int BUFFER_LENGTH = 4096;

    /**
     * Constructs an instance only with the request URL
     *
     * @param context    The current Context
     * @param requestUrl The HTTP request URL
     */
    public HttpUrlMultiPartRequest(Context context, String requestUrl) {
        super(context, requestUrl);
        this.mRequestParameterBytesCollection = new ArrayList<byte[]>();
        this.mFileUploadContentUris = new HashMap<String, Uri>();
        this.mFileUploadHeaderBytesCollection = new HashMap<Uri, byte[]>();
    }

    /**
     * Constructs an instance only with the request URL and authentication header
     *
     * @param context       The current Context
     * @param requestUrl    The HTTP request URL
     * @param account       The Account that will be used to get the authentication token
     * @param authTokenType The type of authentication token
     */
    public HttpUrlMultiPartRequest(Context context, String requestUrl, Account account,
                                   String authTokenType) {
        super(context, requestUrl, account, authTokenType);
        this.mRequestParameterBytesCollection = new ArrayList<byte[]>();
        this.mFileUploadContentUris = new HashMap<String, Uri>();
        this.mFileUploadHeaderBytesCollection = new HashMap<Uri, byte[]>();
    }

    /**
     * Sets up the request by specifying the Content-Type header as a multi-part request.  Also
     * converts all parts of the request body to bytes.
     *
     * @param httpUrlConnection The HTTP request object that will be setup
     * @throws ProtocolException
     */
    @Override
    protected void setupRequest(HttpURLConnection httpUrlConnection) throws ProtocolException {
        try {
            // The request method is POST for multipart requests
            httpUrlConnection.setRequestMethod(HttpPost.METHOD_NAME);
            // No caching
            httpUrlConnection.setUseCaches(false);
            // Flag that there will be input and output
            httpUrlConnection.setDoInput(true);
            httpUrlConnection.setDoOutput(true);
            // Keep the connection alive
            this.addRequestHeader("Connection", "Keep-Alive");
            // Generate the boundary for splitting up the request parts
            this.mBoundary = this.generateBoundary();
            // Set the content type as multipart form data
            this.addRequestHeader("Content-Type", "multipart/form-data; boundary="
                    + this.mBoundary);

            // Get the boundary string as bytes
            this.mBoundaryBytes = this.getBoundaryAsBytes(this.mBoundary);
            this.mFinalBoundaryBytes = this.getFinalBoundaryAsBytes(this.mBoundary);

            // Convert the request parameters and file upload request body headers to bytes
            this.convertRequestParametersToBytes();
            this.convertFileUploadsToBytes();
        } catch (IOException e) {
        }
    }

    /**
     * Determines the total content length of the request body by summing the byte counts of the
     * HTTP request parameters, the file uploads, and all of the boundary strings separating
     * the different parts of the HTTP request
     *
     * @return The total content length of the HTTP request body
     */
    @Override
    protected long determineRequestBodyLength() {
        // Determine the byte count overhead of adding a boundary for each request parameter
        final long requestParametersBoundaryByteCount =
                this.mBoundaryBytes.length * this.mRequestParameterBytesCollection.size();

        // Determine the byte count overhead of adding a boundary for each file upload
        final long fileUploadBoundaryByteCount =
                this.mBoundaryBytes.length * this.mFileUploadHeaderBytesCollection.size();

        // Sum all of the byte counts to get the total size of the request body
        return this.mTotalRequestParameterByteCount + requestParametersBoundaryByteCount
                + this.mTotalFileUploadByteCount + fileUploadBoundaryByteCount
                + this.mFinalBoundaryBytes.length;
    }

    /**
     * Writes all parts of the HTTP request body to the request stream, including the request
     * parameters, the file uploads, and the multi-part request boundaries
     */
    @Override
    protected void writeToRequestStream() {
        try {
            // Write the request parameters to the request stream
            this.writeRequestParametersToStream(this.mRequestStream,
                    this.mRequestParameterBytesCollection);
            // Write the file uploads to the request stream
            this.writeFileUploadsToStream(this.mRequestStream,
                    this.mFileUploadHeaderBytesCollection);
            // Write the final boundary to the stream
            this.mRequestStream.write(this.mFinalBoundaryBytes);
            // Notify the listener tracking the amount of data sent
            this.notifyDataSentListener(this.mFinalBoundaryBytes.length, this.mRequestBodyLength);
        } catch (IOException e) {
        }
    }

    /**
     * Adds a FileProvider content URI to be uploaded in the HTTP request and its corresponding
     * parameter name
     *
     * @param parameter The HTTP request parameter name for the file upload
     * @param uri       The FileProvider content URI of the file to be uploaded
     */
    public void addFileUploadContentUri(String parameter, Uri uri) {
        if (parameter == null || uri == null) {
            throw new IllegalArgumentException("Parameter name or uri cannot be null");
        }
        this.mFileUploadContentUris.put(parameter, uri);
    }

    /**
     * Generates a boundary for the mutli-part request
     *
     * @return A boundary String
     */
    private String generateBoundary() {
        return String.valueOf(System.currentTimeMillis());
    }

    /**
     * Returns the specified boundary as bytes
     *
     * @param boundary The boundary String to return as bytes
     * @return The boundary String as bytes
     * @throws UnsupportedEncodingException
     */
    private byte[] getBoundaryAsBytes(String boundary) throws UnsupportedEncodingException {
        String fullBoundary = NEW_LINE + TWO_HYPHENS + boundary + NEW_LINE;

        return fullBoundary.getBytes(US_ASCII);
    }

    /**
     * Returns the final boundary as bytes which is defined to be the in the format:
     * --[boundary]--
     *
     * @param boundary The boundary String
     * @return The final boundary String as bytes
     * @throws UnsupportedEncodingException
     */
    private byte[] getFinalBoundaryAsBytes(String boundary) throws UnsupportedEncodingException {
        String fullBoundary = NEW_LINE + TWO_HYPHENS + boundary + TWO_HYPHENS + NEW_LINE;

        return fullBoundary.getBytes(US_ASCII);
    }

    /**
     * Converts all request parameters in the collection to bytes and determines the total byte
     * count of all the request parameters
     */
    protected void convertRequestParametersToBytes() {
        for (Pair<String, String> requestParameter : this.mRequestParameters) {
            try {
                // Get the request parameter as bytes
                byte[] requestParameterBytes = this.getRequestParameterAsBytes(
                        requestParameter.first, requestParameter.second);
                // Append the byte count to the total request parameter byte count
                this.mTotalRequestParameterByteCount += requestParameterBytes.length;
                // Add the request parameter bytes to the collection
                this.mRequestParameterBytesCollection.add(requestParameterBytes);
            } catch (UnsupportedEncodingException e) {
            }
        }
    }

    /**
     * Converts all file uploads headers to bytes and determines the total byte count of all
     * the file upload headers and corresponding file sizes
     */
    private void convertFileUploadsToBytes() {
        for (Map.Entry<String, Uri> entry : this.mFileUploadContentUris.entrySet()) {
            try {
                // Get the header as bytes that will be placed in the request body for the given file
                byte[] fileUploadHeaderBytes = this.getFileUploadHeaderAsBytes(entry.getKey(),
                        entry.getValue());
                // Get the file size in bytes of the file from the FileProvider
                final long fileSize = Files.getFileSize(this.mContext, entry.getValue());
                // Add the byte count of the header to be used in the request body and also the
                // size of the file itself to the total file upload byte count
                this.mTotalFileUploadByteCount += fileUploadHeaderBytes.length + fileSize;
                // Add the file upload header bytes to a collection along with the corresponding URI
                this.mFileUploadHeaderBytesCollection.put(entry.getValue(), fileUploadHeaderBytes);
            } catch (UnsupportedEncodingException e) {
            }
        }
    }

    /**
     * Builds a String representing a request parameter name and value as it would appear in a
     * multi-part request and then converts it to bytes
     *
     * @param parameter The request parameter name
     * @param value     The request parameter value
     * @return The request parameter and value in multi-part request form as bytes
     * @throws UnsupportedEncodingException
     */
    private byte[] getRequestParameterAsBytes(String parameter, String value)
            throws UnsupportedEncodingException {
        final StringBuilder builder = new StringBuilder();

        builder.append(String.format("Content-Disposition: form-data; name=\"%1$s\"", parameter))
                .append(NEW_LINE).append(NEW_LINE)
                .append(value);

        return builder.toString().getBytes(UTF_8);
    }

    /**
     * Builds a String representing the header that will precede a file's contents in a
     * multi-part request and converts it to bytes
     *
     * @param parameter The parameter name of the file upload
     * @param uri       The FileProvider content URI of the file to be uploaded
     * @return The header that will precede a file's contents in a multi-part request as bytes
     * @throws UnsupportedEncodingException
     */
    private byte[] getFileUploadHeaderAsBytes(String parameter, Uri uri)
            throws UnsupportedEncodingException {
        // Get the filename
        final String filename = UUID.randomUUID().toString();
        // Get the mime type
        final String mimeType = this.mContext.getContentResolver().getType(uri);

        // Build the header for the file that will be placed in the request body
        final StringBuilder builder = new StringBuilder();
        builder.append(
                String.format("Content-Disposition: form-data; name=\"%1$s\"; filename=\"%2$s\"",
                        parameter, filename))
                .append(NEW_LINE)
                .append(String.format("Content-Type: %1$s", mimeType))
                .append(NEW_LINE).append(NEW_LINE);

        return builder.toString().getBytes(US_ASCII);
    }

    /**
     * Writes the collection of bytes representing the request parameters to the HTTP requesst
     * stream
     *
     * @param outputStream                    The HTTP request stream
     * @param requestParameterBytesCollection The collection of bytes representing the request parameters
     */
    private void writeRequestParametersToStream(OutputStream outputStream,
                                                List<byte[]> requestParameterBytesCollection) {
        for (byte[] requestParameterBytes : requestParameterBytesCollection) {
            try {
                // Write the bytes representing the boundary
                outputStream.write(this.mBoundaryBytes);
                // Write the bytes representing the request parameter
                outputStream.write(requestParameterBytes);
                // Notify the listener tracking the amount of data sent
                this.notifyDataSentListener(this.mBoundaryBytes.length
                        + requestParameterBytes.length, this.mRequestBodyLength);
            } catch (IOException e) {
            }
        }
    }

    /**
     * Writes the bytes of headers that precede files in a multi-part request as well as the
     * corresponding file contents themselves to the HTTP request stream
     *
     * @param outputStream                    The HTTP request stream that the file bytes will be written to
     * @param fileUploadHeaderBytesCollection Collection mapping of FileProvider content URIs to
     *                                        bytes that represent the header for the file in the
     *                                        multi-part HTTP request
     */
    private void writeFileUploadsToStream(OutputStream outputStream,
                                          Map<Uri, byte[]> fileUploadHeaderBytesCollection) {
        for (Map.Entry<Uri, byte[]> entry : fileUploadHeaderBytesCollection.entrySet()) {
            InputStream fileInputStream = null;
            try {
                // Write the bytes representing the boundary
                outputStream.write(this.mBoundaryBytes);
                // Write the bytes representing the request body header for the file
                outputStream.write(entry.getValue());
                // Notify the listener tracking the amount of data sent
                this.notifyDataSentListener(this.mBoundaryBytes.length + entry.getValue().length,
                        this.mRequestBodyLength);

                // Write the bytes of the file contents
                fileInputStream = this.mContext.getContentResolver()
                        .openInputStream(entry.getKey());
                // Read the file in parts
                byte[] buffer = new byte[BUFFER_LENGTH];
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, bytesRead);
                    // Notify the listener tracking the amount of data sent
                    this.notifyDataSentListener(bytesRead, this.mRequestBodyLength);
                }
            } catch (IOException e) {
            } finally {
                this.closeInputStream(fileInputStream);
            }
        }
    }

}
