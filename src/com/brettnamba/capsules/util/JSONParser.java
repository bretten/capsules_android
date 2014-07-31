package com.brettnamba.capsules.util;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.brettnamba.capsules.dataaccess.Capsule;
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
     * Parses an authentication response to retrieve an authentication token.
     * 
     * @param body
     * @return String
     * @throws JSONException
     */
    public static String parseAuthenticationToken(String body) throws JSONException {
        // Parse the response
        JSONObject json = new JSONObject(body);

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

        // Parse individual JSON objects
        for (int i = 0; i < json.length(); i++) {
            JSONObject jsonCapsule = json.getJSONObject(i).getJSONObject(RequestContract.Field.CAPSULE_OBJECT);

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

}
