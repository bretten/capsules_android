package com.brettnamba.capsules.util;

import com.brettnamba.capsules.dataaccess.Capsule;
import com.brettnamba.capsules.dataaccess.CapsuleDiscovery;
import com.brettnamba.capsules.dataaccess.CapsuleOwnership;
import com.brettnamba.capsules.dataaccess.Discovery;
import com.brettnamba.capsules.dataaccess.Memoir;
import com.brettnamba.capsules.dataaccess.User;
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
public final class JSONParser {

    /**
     * Private constructor to prevent instantiation
     */
    private JSONParser() {
    }

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
     * Parses the messages out of a API response for saving a Capsule
     *
     * @param jsonObject JSONObject representing a Web API response
     * @return Collection of flattened messages
     * @throws JSONException
     */
    public static List<String> parseSaveCapsuleMessages(JSONObject jsonObject)
            throws JSONException {
        // Will hold the messages
        List<String> messages = new ArrayList<String>();

        // Make sure there is a messages key
        if (!jsonObject.has(RequestContract.Field.MESSAGES)) {
            return messages;
        }

        // Get the message object
        JSONObject jsonMessages = jsonObject.getJSONObject(RequestContract.Field.MESSAGES);

        // Check for the Capsule name key
        if (jsonMessages.has(RequestContract.Field.CAPSULE_NAME)) {
            // Get the name key messages
            JSONArray capsuleNameMessages = jsonMessages.getJSONArray(
                    RequestContract.Field.CAPSULE_NAME);
            for (int i = 0; i < capsuleNameMessages.length(); i++) {
                messages.add(capsuleNameMessages.getString(i));
            }
        }

        // Check for the Memoir key
        if (!jsonMessages.has(RequestContract.Field.MEMOIR_ENTITY)) {
            return messages;
        }

        // Get the Memoir array
        JSONArray jsonMemoirs = jsonMessages.getJSONArray(RequestContract.Field.MEMOIR_ENTITY);

        // Validate the Memoirs
        for (int i = 0; i < jsonMemoirs.length(); i++) {
            JSONObject jsonMemoir = jsonMemoirs.getJSONObject(i);
            // Check for the Memoir title key
            if (jsonMemoir.has(RequestContract.Field.MEMOIR_TITLE)) {
                // Get the Memoir title messages
                JSONArray memoirTitleMessages = jsonMemoir.getJSONArray(
                        RequestContract.Field.MEMOIR_TITLE);
                for (int j = 0; j < memoirTitleMessages.length(); j++) {
                    messages.add(memoirTitleMessages.getString(j));
                }
            }
            // Check for the Memoir message key
            if (jsonMemoir.has(RequestContract.Field.MEMOIR_MESSAGE)) {
                // Get the Memoir message messages
                JSONArray memoirMessageMessages = jsonMemoir.getJSONArray(
                        RequestContract.Field.MEMOIR_MESSAGE);
                for (int j = 0; j < memoirMessageMessages.length(); j++) {
                    messages.add(memoirMessageMessages.getString(j));
                }
            }
            // Check for the Memoir file key
            if (jsonMemoir.has(RequestContract.Field.MEMOIR_FILE)) {
                // Get the Memoir file messages
                JSONArray memoirFileMessages = jsonMemoir.getJSONArray(
                        RequestContract.Field.MEMOIR_FILE);
                for (int j = 0; j < memoirFileMessages.length(); j++) {
                    messages.add(memoirFileMessages.getString(j));
                }
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
     * Parses a ctag from a JSON server response
     *
     * @param json JSONObject representing a Web API JSON response
     * @return A string Ctag
     * @throws JSONException
     */
    public static String parseCtag(JSONObject json) throws JSONException {
        if (!json.has(RequestContract.Field.DATA)) {
            return null;
        }

        // Get the JSON data object
        JSONObject dataObject = json.getJSONObject(RequestContract.Field.DATA);

        // Return the token if it exists
        if (dataObject.has(RequestContract.Field.CTAG)) {
            return dataObject.getString(RequestContract.Field.CTAG);
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
     * Parses a collection of Capsules from a JSONObject
     *
     * @param json The JSONObject
     * @return A collection of Capsules parsed from the JSONObject
     * @throws JSONException
     */
    public static List<Capsule> parseCapsules(JSONObject json) throws JSONException {
        // Will hold the Capsule objects
        List<Capsule> capsules = new ArrayList<Capsule>();

        // Make sure the JSON object has the resource key
        if (!json.has(RequestContract.Field.DATA)) {
            return capsules;
        }

        // Get the JSON data object
        JSONObject data = json.getJSONObject(RequestContract.Field.DATA);

        // Make sure the Capsules key exists
        if (!data.has(RequestContract.Field.CAPSULE_COLLECTION)) {
            return capsules;
        }

        // Get the array of Capsules from the data object
        JSONArray jsonCapsules = data.getJSONArray(RequestContract.Field.CAPSULE_COLLECTION);
        // Iterate through the array and build Capsules from the JSON objects
        for (int i = 0; i < jsonCapsules.length(); i++) {
            // Get the Capsule entry
            JSONObject entry = jsonCapsules.getJSONObject(i);
            if (!entry.has(RequestContract.Field.CAPSULE_ENTITY)) {
                continue;
            }
            // Get the Capsule object
            JSONObject jsonCapsule = entry.getJSONObject(RequestContract.Field.CAPSULE_ENTITY);
            // Parse the Capsule
            Capsule capsule = new Capsule(jsonCapsule);

            // Parse the Memoir if there is one
            if (entry.has(RequestContract.Field.MEMOIR_ENTITY)) {
                // Get the Memoir object
                JSONObject jsonMemoir = entry.getJSONObject(RequestContract.Field.MEMOIR_ENTITY);
                // Parse the Memoir and add it to the Capsule
                capsule.setMemoir(new Memoir(jsonMemoir));
            }

            // Parse the Discovery if there is one
            if (entry.has(RequestContract.Field.DISCOVERY_ENTITY)) {
                // Get the Discovery object
                JSONObject jsonDiscovery =
                        entry.getJSONObject(RequestContract.Field.DISCOVERY_ENTITY);
                // Parse the Discovery and add it to the Capsule
                capsule.setDiscovery(new Discovery(jsonDiscovery));
            }

            // Parse the User if there is one
            if (entry.has(RequestContract.Field.USER_ENTITY)) {
                // Get the User object
                JSONObject jsonUser = entry.getJSONObject(RequestContract.Field.USER_ENTITY);
                // Parse the User and add it to the Capsule
                capsule.setUser(new User(jsonUser));
            }

            // Add the Capsule to the collection
            capsules.add(capsule);
        }

        return capsules;
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
                    capsules.add(new Capsule(jsonCapsule));
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
    public static CapsuleDiscovery parseOpenCapsule(JSONObject json) throws JSONException {
        // The Capsule that was opened
        CapsuleDiscovery capsule = null;

        // Check for the resource data key
        if (json.has(RequestContract.Field.DATA)) {
            // Get the resource data object
            JSONObject data = json.getJSONObject(RequestContract.Field.DATA);
            // Check for the Capsule object
            if (data.has(RequestContract.Field.DISCOVERY)) {
                // Get the JSON Capsule object
                JSONObject jsonCapsule = data.getJSONObject(RequestContract.Field.DISCOVERY);
                // Get the Capsule data from the JSON
                capsule = new CapsuleDiscovery(jsonCapsule);
            }
        }

        return capsule;
    }

    /**
     * Parses a JSON server response containing a single CapsuleOwnership
     *
     * @param json The JSON response body
     * @return The Capsule if it was in the response, otherwise null
     * @throws JSONException
     */
    public static CapsuleOwnership parseOwnershipCapsule(JSONObject json) throws JSONException {
        // The Capsule
        CapsuleOwnership capsule = null;

        // Check for the resource data key
        if (json.has(RequestContract.Field.DATA)) {
            // Get the resource data object
            JSONObject data = json.getJSONObject(RequestContract.Field.DATA);
            // Check for the Capsule object
            if (data.has(RequestContract.Field.CAPSULE)) {
                // Get the JSON Capsule object
                JSONObject jsonCapsule = data.getJSONObject(RequestContract.Field.CAPSULE);
                // Get the Capsule data from the JSON
                capsule = new CapsuleOwnership(jsonCapsule);
            }
        }

        return capsule;
    }

    /**
     * Parses a JSON server response containing a collection of Capsule ownerships
     *
     * @param json The JSON response body
     * @return The collection of Capsules from the response
     * @throws JSONException
     */
    public static List<CapsuleOwnership> parseOwnershipCollection(JSONObject json) throws JSONException {
        // Will hold the Capsule objects
        List<CapsuleOwnership> capsules = new ArrayList<CapsuleOwnership>();

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
                    capsules.add(new CapsuleOwnership(jsonCapsule));
                }
            }
        }

        return capsules;
    }

}
