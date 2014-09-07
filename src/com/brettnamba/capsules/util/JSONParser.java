package com.brettnamba.capsules.util;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.brettnamba.capsules.dataaccess.Capsule;
import com.brettnamba.capsules.dataaccess.CapsuleOwnershipPojo;
import com.brettnamba.capsules.dataaccess.CapsulePojo;
import com.brettnamba.capsules.http.RequestContract;

/**
 * Handles parsing server responses into application-specific data structures.
 * 
 * @author Brett
 *
 */
public class JSONParser {

    /**
     * The name of the JSONObject that contains a web resource's data.
     */
    public static final String RESOURCE_DATA = "data";

    /**
     * Parses an authentication response to retrieve an authentication token.
     * 
     * @param body
     * @return String
     * @throws JSONException
     */
    public static String parseAuthenticationToken(String body) throws JSONException {
        // Parse the response
        JSONObject json = new JSONObject(body).getJSONObject(RESOURCE_DATA);

        return json.getString(RequestContract.Field.AUTH_TOKEN_RESPONSE);
    }

    /**
     * Parses a response for undiscovered Capsules and places them in a Collection.
     * 
     * @param body
     * @return List<Capsule>
     * @throws JSONException
     */
    public static List<Capsule> parseUndiscoveredCapsules(String body) throws JSONException {
        // Parse the response
        JSONArray json = new JSONArray(body);

        // Will hold the Capsule objects
        List<Capsule> capsules = new ArrayList<Capsule>();

        // Extract data from individual JSON objects
        for (int i = 0; i < json.length(); i++) {
            JSONObject jsonCapsule = json.getJSONObject(i).getJSONObject(RESOURCE_DATA);

            // Create Capsules
            capsules.add(new CapsulePojo()
                .setSyncId(jsonCapsule.getLong(RequestContract.Field.CAPSULE_SYNC_ID))
                .setName(jsonCapsule.getString(RequestContract.Field.CAPSULE_NAME))
                .setLatitude(jsonCapsule.getDouble(RequestContract.Field.CAPSULE_LATITUDE))
                .setLongitude(jsonCapsule.getDouble(RequestContract.Field.CAPSULE_LONGITUDE))
            );
        }

        return capsules;
    }

    /**
     * Parses an "open Capsule" response.
     * 
     * @param body
     * @return
     * @throws JSONException
     */
    public static String parseOpenCapsule(String body) throws JSONException {
        // Parse the response
        JSONObject json = new JSONObject(body).getJSONObject(RESOURCE_DATA);

        return json.getString(RequestContract.Field.CAPSULE_ETAG);
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
        JSONObject json = new JSONObject(body).getJSONObject(RESOURCE_DATA);

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
            JSONObject jsonCapsule = json.getJSONObject(i).getJSONObject(RESOURCE_DATA);

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
            JSONObject jsonCapsule = json.getJSONObject(i).getJSONObject(RESOURCE_DATA);

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
