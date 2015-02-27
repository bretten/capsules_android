package com.brettnamba.capsules.http;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

/**
 * Factory class for creating an instance of an HTTP client.
 * 
 * @author Brett Namba
 *
 */
public class HttpFactory {

    /**
     * The default timeout value used for HTTP connections.
     */
    public static final int TIMEOUT = 30 * 1000;

    /**
     * Factory method for an instance of HttpClient.
     * 
     * @return HttpClient
     */
    public static HttpClient getInstance() {
        // HTTP parameters
        final HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, TIMEOUT);
        ConnManagerParams.setTimeout(params, TIMEOUT);

        // Scheme registry
        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

        // HTTP client
        ClientConnectionManager manager = new ThreadSafeClientConnManager(params, registry);
        HttpClient client = new DefaultHttpClient(manager, params);

        return client;
    }

}
