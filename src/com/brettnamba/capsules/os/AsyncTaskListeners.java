package com.brettnamba.capsules.os;

import android.accounts.Account;
import android.graphics.Bitmap;

import com.brettnamba.capsules.dataaccess.Capsule;
import com.brettnamba.capsules.dataaccess.CapsuleOwnership;
import com.brettnamba.capsules.dataaccess.Discovery;
import com.brettnamba.capsules.http.CapsuleRequestParameters;
import com.brettnamba.capsules.http.response.CapsuleOpenResponse;
import com.brettnamba.capsules.http.response.CapsulePingResponse;
import com.brettnamba.capsules.http.response.JsonResponse;
import com.brettnamba.tomoeame.os.AsyncListenerTask;

import java.util.List;

/**
 * Listener interfaces that outline the events on background thread tasks and allow other classes
 * to handle their events.
 *
 * @author Brett Namba
 */
public class AsyncTaskListeners {

    /**
     * Listener for any AsyncTasks that perform authentication using AuthenticationResponse
     */
    public interface AuthenticationTaskListener extends AsyncListenerTask.TaskListener {
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
    public interface AuthTokenRetrievalTaskListener extends AsyncListenerTask.TaskListener {
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
    public interface CapsulePingTaskListener extends AsyncListenerTask.TaskListener {
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
    public interface CapsuleOpenTaskListener extends AsyncListenerTask.TaskListener {
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
    public interface UpdateDiscoveryTaskListener extends AsyncListenerTask.TaskListener {
        /**
         * Should handle doInBackground() work
         *
         * @param params The update data for the Discovery
         * @return The result of the update
         */
        boolean duringUpdateDiscovery(Discovery... params);

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
    public interface OwnershipSaveTaskListener extends AsyncListenerTask.TaskListener {
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
    public interface GetCapsulesTaskListener extends AsyncListenerTask.TaskListener {

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

    /**
     * Listener that handles requesting a Memoir's image data from the server and building a Bitmap
     */
    public interface GetMemoirBitmapTaskListener extends AsyncListenerTask.TaskListener {

        /**
         * Should handle doInBackground() work
         *
         * @param memoirId The server-side ID of the Memoir
         * @return The Memoir's Bitmap
         */
        Bitmap duringGetMemoirBitmap(long memoirId);

        /**
         * Should handle onPreExecute() work
         */
        void onPreGetMemoirBitmap();

        /**
         * Should handle onPostExecute() work
         *
         * @param bitmap The constructed Bitmap from the server's image data
         */
        void onPostGetMemoirBitmap(Bitmap bitmap);

        /**
         * Should handle onCancelled() work
         */
        void onGetMemoirBitmapCancelled();

    }

    /**
     * Listener that handles callbacks for a request to the server to discover all Capsules
     * nearby the device's location
     */
    public interface DiscoverCapsulesTaskListener extends AsyncListenerTask.TaskListener {

        /**
         * Should handle doInBackground() work
         *
         * @param params The latitude and longitude of the device's location
         * @return The Capsules that were discovered
         */
        List<Capsule> duringDiscoverCapsules(Double... params);

        /**
         * Should handle onPreExecute() work
         */
        void onPreDiscoverCapsules();

        /**
         * Should handle onPostExecute() work
         *
         * @param capsules The Capsules that were discovered
         */
        void onPostDiscoverCapsules(List<Capsule> capsules);

        /**
         * Should handle onCancelled() work
         */
        void onDiscoverCapsulesCancelled();

    }

}
