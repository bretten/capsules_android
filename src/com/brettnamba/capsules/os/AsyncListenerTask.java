package com.brettnamba.capsules.os;

import android.accounts.Account;
import android.os.AsyncTask;

import com.brettnamba.capsules.dataaccess.Capsule;
import com.brettnamba.capsules.dataaccess.CapsuleDiscovery;
import com.brettnamba.capsules.dataaccess.CapsuleOwnership;
import com.brettnamba.capsules.http.CapsuleRequestParameters;
import com.brettnamba.capsules.http.response.CapsuleOpenResponse;
import com.brettnamba.capsules.http.response.CapsulePingResponse;
import com.brettnamba.capsules.http.response.JsonResponse;

import java.util.List;

/**
 * Abstraction of AsyncTask that is meant to use listeners when the
 * standard AsyncTask's methods are invoked
 * <p/>
 * In order to allow an Activity implement multiple listeners, a listener interface
 * is created for each Task so the callbacks can be differentiated.
 *
 * @param <Params>
 * @param <Progress>
 * @param <Result>
 */
public abstract class AsyncListenerTask<Params, Progress, Result>
        extends AsyncTask<Params, Progress, Result> {

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
    public interface TaskListener {
    }

    /**
     * Listener for any AsyncTasks that perform authentication using AuthenticationResponse
     */
    public interface AuthenticationTaskListener extends TaskListener {
        /**
         * Should handle doInBackground() work
         *
         * @param params Credentials to be used in the authentication HTTP request
         * @return AuthenticationResponse
         */
        JsonResponse duringAuthentication(String... params);

        /**
         * Should handle onPreExecute() work
         */
        void onPreAuthentication();

        /**
         * Should handle onPostExecute() work
         *
         * @param response The HTTP response from the authentication request
         */
        void onPostAuthentication(JsonResponse response);

        /**
         * Should handle onCancelled() work
         */
        void onAuthenticationCancelled();
    }

    /**
     * Listener for an AsyncTask that will retrieve an authentication token
     */
    public interface AuthTokenRetrievalTaskListener extends TaskListener {
        /**
         * Should handle doInBackground() work
         *
         * @param params Account to retrieve an authentication token for
         * @return String The auth token
         */
        String duringAuthTokenRetrieval(Account... params);

        /**
         * Should handle onPreExecute() work
         */
        void onPreAuthTokenRetrieval();

        /**
         * Should handle onPostExecute() work
         *
         * @param authToken The retrieved authentication token
         */
        void onPostAuthTokenRetrieval(String authToken);

        /**
         * Should handle onCancelled() work
         */
        void onAuthTokenRetrievalCancelled();
    }

    /**
     * Listener for AsyncTask that will request undiscovered Capsules from the API
     */
    public interface CapsulePingTaskListener extends TaskListener {
        /**
         * Should handle doInBackground() work
         *
         * @param params User's authentication and location information
         * @return HTTP response object
         */
        CapsulePingResponse duringCapsulePing(String... params);

        /**
         * Should handle onPreExecute() work
         */
        void onPreCapsulePing();

        /**
         * Should handle onPostExecute() work
         *
         * @param response HTTP response object for a Capsule ping
         */
        void onPostCapsulePing(CapsulePingResponse response);

        /**
         * Should handle onCancelled() work
         */
        void onCapsulePingCancelled();
    }

    /**
     * Listener for AsyncTask that will make an HTTP request to open a Capsule
     */
    public interface CapsuleOpenTaskListener extends TaskListener {
        /**
         * Should handle doInBackground() work
         *
         * @param params User's authentication, location and the Capsule being opened
         * @return HTTP response object
         */
        CapsuleOpenResponse duringCapsuleOpen(String... params);

        /**
         * Should handle onPreExecute() work
         */
        void onPreCapsuleOpen();

        /**
         * Should handle onPostExecute() work
         *
         * @param response HTTP response object from opening the Capsule
         */
        void onPostCapsuleOpen(CapsuleOpenResponse response);

        /**
         * Should handle onCancelled() work
         */
        void onCapsuleOpenCancelled();
    }

    /**
     * Listener for AsyncTask that handles updating a Discovery
     */
    public interface UpdateDiscoveryTaskListener extends TaskListener {
        /**
         * Should handle doInBackground() work
         *
         * @param params The update data for the Discovery
         * @return The result of the update
         */
        boolean duringUpdateDiscovery(CapsuleDiscovery... params);

        /**
         * Should handle onPreExecute() work
         */
        void onPreUpdateDiscovery();

        /**
         * Should handle onPostExecute() work
         *
         * @param result The result of the update
         */
        void onPostUpdateDiscovery(Boolean result);

        /**
         * Should handle onCancelled() work
         */
        void onUpdateDiscoveryCancelled();
    }

    /**
     * Listener for AsyncTask that handles saving a Capsule ownership
     */
    public interface OwnershipSaveTaskListener extends TaskListener {
        /**
         * Should handle doInBackground() work
         *
         * @param params The Capsule being saved
         * @return The result of the save
         */
        boolean duringOwnershipSave(CapsuleOwnership... params);

        /**
         * Should handle onPreExecute() work
         */
        void onPreOwnershipSave();

        /**
         * Should handle onPostExecute() work
         *
         * @param success The result of the save
         */
        void onPostOwnershipSave(Boolean success);

        /**
         * Should handle onCancelled() work
         */
        void onOwnershipSaveCancelled();
    }

    /**
     * Listener for AsyncTask that handles requesting Capsules from the server and parsing
     * the results
     */
    public interface GetCapsulesTaskListener extends TaskListener {

        /**
         * Should handle doInBackground() work
         *
         * @param params Parameters for the request
         * @return The parsed collection of Capsules
         */
        List<Capsule> duringGetCapsules(CapsuleRequestParameters params);

        /**
         * Should handle onPreExecute() work
         */
        void onPreGetCapsules();

        /**
         * Should handle onPostExecute() work
         *
         * @param capsules The parsed collection of Capsules returned from the request
         */
        void onPostGetCapsules(List<Capsule> capsules);

        /**
         * Should handle onCancelled() work
         */
        void onGetCapsulesCancelled();

    }

}
