package com.brettnamba.capsules.os;

import android.accounts.Account;
import android.os.AsyncTask;

import com.brettnamba.capsules.http.response.AuthenticationResponse;

/**
 * Abstraction of AsyncTask that is meant to use listeners when the
 * standard AsyncTask's methods are invoked
 *
 * In order to allow an Activity implement multiple listeners, a listener interface
 * is created for each Task so the callbacks can be differentiated.
 *
 * @param <Params>
 * @param <Progress>
 * @param <Result>
 */
public abstract class AsyncListenerTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

    /**
     * Sets the listener for the AsyncTask
     *
     * @param listener The listener that handles the callbacks
     */
    public abstract void setListener(TaskListener listener);

    /**
     * Removes the listener that handles the callbacks
     */
    public abstract void removeListener();

    /**
     * Base TaskListener interface that all listeners should extend from
     */
    public interface TaskListener {}

    /**
     * Listener for any AsyncTasks that perform authentication using AuthenticationResponse
     */
    public interface AuthenticationTaskListener extends TaskListener {
        /**
         * To be called from doInBackground()
         *
         * @param params Credentials to be used in the authentication HTTP request
         * @return AuthenticationResponse
         */
        AuthenticationResponse duringAuthentication(String... params);

        /**
         * Should be called from onPreExecute()
         */
        void onPreAuthentication();

        /**
         * Should be called from onPostExecute()
         *
         * @param response The HTTP response from the authentication request
         */
        void onPostAuthentication(AuthenticationResponse response);

        /**
         * Should be called from onCancelled()
         */
        void onAuthenticationCancelled();
    }

    /**
     * Listener for an AsyncTask that will retrieve an authentication token
     */
    public interface AuthTokenRetrievalTaskListener extends TaskListener {
        /**
         * To be called from doInBackground()
         *
         * @param params Account to retrieve an authentication token for
         * @return String The auth token
         */
        String duringAuthTokenRetrieval(Account... params);

        /**
         * Should be called from onPreExecute()
         */
        void onPreAuthTokenRetrieval();

        /**
         * Should be called from onPostExecute()
         *
         * @param authToken The retrieved authentication token
         */
        void onPostAuthTokenRetrieval(String authToken);

        /**
         * Should be called from onCancelled()
         */
        void onAuthTokenRetrievalCancelled();
    }

}
