package com.brettnamba.capsules.util;

import com.brettnamba.capsules.dataaccess.Capsule;
import com.brettnamba.capsules.dataaccess.CapsuleDiscoveryPojo;
import com.brettnamba.capsules.dataaccess.CapsuleOwnershipPojo;
import com.brettnamba.capsules.dataaccess.CapsulePojo;
import com.brettnamba.capsules.http.RequestContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles parsing server responses into application-specific data structures.
 *
 * @author Brett Namba
 */
public class JSONParser {

    /**
     * Parses API response messages from a JSONObject
     *
     * @param jsonObject JSONObject representing a Web API JSON response
     * @return Collection of messages
     * @throws JSONException
     */
    public static List<String> parseMessages(JSONObject jsonObject) throws JSONException {
        // Will hold the messages
        List<String> messages = new ArrayList<String>();

        // Get the messages
        if (jsonObject.has(RequestContract.Field.MESSAGES)) {
            JSONArray jsonMessages = jsonObject.getJSONArray(RequestContract.Field.MESSAGES);
            for (int i = 0; i < jsonMessages.length(); i++) {
                messages.add(jsonMessages.getString(i));
            }
        }

        return messages;
    }

    /**
     * Parses an authentication token from a JSONObject
     *
     * @param jsonObject JSONObject representing a Web API JSON response
     * @return String An authentication token
     * @throws JSONException
     */
    public static String parseAuthToken(JSONObject jsonObject) throws JSONException {
        if (!jsonObject.has(RequestContract.Field.DATA)) {
            return null;
        }

        // Get the JSON data object
        JSONObject dataObject = jsonObject.getJSONObject(RequestContract.Field.DATA);

        // Return the token if it exists
        if (dataObject.has(RequestContract.Field.AUTH_TOKEN_RESPONSE)) {
            return dataObject.getString(RequestContract.Field.AUTH_TOKEN_RESPONSE);
        } else {
            return null;
        }
    }

    /**
     * Parses an authentication response to retrieve an authentication token.
     *
     * @param body
     * @return String
     * @throws JSONException
     */
    public static String parseAuthenticationToken(String body) throws JSONException {
        // Parse the response
        JSONObject json = new JSONObject(body).getJSONObject(RequestContract.Field.DATA);

        return json.getString(RequestContract.Field.AUTH_TOKEN_RESPONSE);
    }

    /**
     * Parses a JSON response from the API that holds undiscovered Capsules
     *
     * @param json JSON object created from the HTTP response
     * @return A collection of undiscovered Capsules
     * @throws JSONException
     */
    public static List<Capsule> parseUndiscoveredCapsules(JSONObject json) throws JSONException {
        // Will hold the Capsule objects
        List<Capsule> capsules = new ArrayList<Capsule>();

        // Make sure the JSON object has the resource key
        if (json.has(RequestContract.Field.DATA)) {
            // Get the JSON data object
            JSONObject data = json.getJSONObject(RequestContract.Field.DATA);
            // Make sure the Capsules key exists
            if (data.has(RequestContract.Field.CAPSULE_COLLECTION)) {
                // Get the array of Capsules from the data object
                JSONArray jsonCapsules = data.getJSONArray(RequestContract.Field.CAPSULE_COLLECTION);
                // Iterate through the array and build Capsules from the JSON objects
                for (int i = 0; i < jsonCapsules.length(); i++) {
                    JSONObject jsonCapsule = jsonCapsules.getJSONObject(i);
                    // Parse the Capsule and add it to the collection
                    capsules.add(new CapsulePojo()
                                    .setSyncId(jsonCapsule.getLong(RequestContract.Field.CAPSULE_SYNC_ID))
                                    .setName(jsonCapsule.getString(RequestContract.Field.CAPSULE_NAME))
                                    .setLatitude(jsonCapsule.getDouble(RequestContract.Field.CAPSULE_LATITUDE))
                                    .setLongitude(jsonCapsule.getDouble(RequestContract.Field.CAPSULE_LONGITUDE))
                    );
                }
            }
        }

        return capsules;
    }

    /**
     * Parses a JSON response from a Capsule open request
     *
     * @param json The JSON response object
     * @return The newly opened Capsule or null if nothing was opened
     * @throws JSONException
     */
    public static CapsuleDiscoveryPojo parseOpenCapsule(JSONObject json) throws JSONException {
        // The Capsule that was opened
        CapsuleDiscoveryPojo capsule = null;

        // Check for the resource data key
        if (json.has(RequestContract.Field.DATA)) {
            // Get the resource data object
            JSONObject data = json.getJSONObject(RequestContract.Field.DATA);
            // Check for the Capsule object
            if (data.has(RequestContract.Field.DISCOVERY)) {
                // Get the JSON Capsule object
                JSONObject jsonCapsule = data.getJSONObject(RequestContract.Field.DISCOVERY);
                // Get the Capsule data from the JSON
                capsule = new CapsuleDiscoveryPojo(jsonCapsule);
            }
        }

        return capsule;
    }

    /**
     * Parses a server response for updating an Ownership Capsule.
     *
     * @param body
     * @return
     * @throws JSONException
     */
    public static Capsule parseOwnershipCapsule(String body) throws JSONException {
        // Parse the response
        JSONObject json = new JSONObject(body).getJSONObject(RequestContract.Field.DATA);

        // Build the Capsule
        Capsule capsule = new CapsuleOwnershipPojo();
        ((CapsuleOwnershipPojo) capsule.setSyncId(json.getLong(RequestContract.Field.CAPSULE_SYNC_ID))
                .setName(json.getString(RequestContract.Field.CAPSULE_NAME))
                .setLatitude(json.getDouble(RequestContract.Field.CAPSULE_LATITUDE))
                .setLongitude(json.getDouble(RequestContract.Field.CAPSULE_LONGITUDE)))
                .setEtag(json.getString(RequestContract.Field.CAPSULE_ETAG));

        return capsule;
    }

    /**
     * Parses a server response holding Capsule Ownership status information
     *
     * @param body
     * @return
     * @throws JSONException
     */
    public static List<Capsule> parseOwnershipStatus(String body) throws JSONException {
        // Parse the response
        JSONArray json = new JSONArray(body);

        // Will hold the Capsule objects
        List<Capsule> capsules = new ArrayList<Capsule>();

        // Extract data from individual JSON objects
        for (int i = 0; i < json.length(); i++) {
            JSONObject jsonCapsule = json.getJSONObject(i).getJSONObject(RequestContract.Field.DATA);

            // Create Capsules
            capsules.add(((CapsuleOwnershipPojo) new CapsuleOwnershipPojo()
                            .setSyncId(jsonCapsule.getLong(RequestContract.Field.CAPSULE_SYNC_ID)))
                            .setEtag(jsonCapsule.getString(RequestContract.Field.CAPSULE_ETAG))
            );
        }

        return capsules;
    }

    /**
     * Parses a server response holding a Capsule Ownership REPORT
     *
     * @param body
     * @return
     * @throws JSONException
     */
    public static List<Capsule> parseOwnershipReport(String body) throws JSONException {
        // Parse the response
        JSONArray json = new JSONArray(body);

        // Will hold the Capsule objects
        List<Capsule> capsules = new ArrayList<Capsule>();

        // Extract data from individual JSON objects
        for (int i = 0; i < json.length(); i++) {
            JSONObject jsonCapsule = json.getJSONObject(i).getJSONObject(RequestContract.Field.DATA);

            // Create Capsules
            capsules.add(((CapsuleOwnershipPojo) new CapsuleOwnershipPojo()
                            .setSyncId(jsonCapsule.getLong(RequestContract.Field.CAPSULE_SYNC_ID))
                            .setName(jsonCapsule.getString(RequestContract.Field.CAPSULE_NAME))
                            .setLatitude(jsonCapsule.getDouble(RequestContract.Field.CAPSULE_LATITUDE))
                            .setLongitude(jsonCapsule.getDouble(RequestContract.Field.CAPSULE_LONGITUDE)))
                            .setEtag(jsonCapsule.getString(RequestContract.Field.CAPSULE_ETAG))
            );
        }

        return capsules;
    }

}
