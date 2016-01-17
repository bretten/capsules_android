package com.brettnamba.capsules.services;

import android.accounts.Account;
import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;

import com.brettnamba.capsules.R;
import com.brettnamba.capsules.dataaccess.Capsule;
import com.brettnamba.capsules.http.HttpUrlConnectionRequest;
import com.brettnamba.capsules.http.HttpUrlMultiPartRequest;
import com.brettnamba.capsules.http.HttpUrlWwwFormRequest;
import com.brettnamba.capsules.http.RequestHandler;
import com.brettnamba.capsules.util.JSONParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Service that handles validating, saving a Capsule, and reporting the progress back to the
 * user.
 *
 * @author Brett Namba
 */
public class SaveCapsuleService extends IntentService implements
        HttpUrlConnectionRequest.DataSentListener {

    /**
     * The Capsule being edited
     */
    private Capsule mCapsule;

    /**
     * The current Account
     */
    private Account mAccount;

    /**
     * WakeLock to prevent the device from sleeping while the IntentService is running
     */
    private PowerManager.WakeLock mWakeLock;

    /**
     * NotificationManager for sending notification updates on the status of the IntentService
     */
    private NotificationManager mNotifyManager;

    /**
     * Builder used to create status update notifications
     */
    private NotificationCompat.Builder mNotificationBuilder;

    /**
     * Keeps a record of how many bytes have been uploaded for the Capsule save HTTP request
     */
    private long mTotalBytesUploaded;

    /**
     * Keeps a record of current progression percentage-wise for the Capsule save HTTP request.
     * Used so that the IntentService will only send notification updates whenever the percentage
     * progress has changed by a full percentage point.
     */
    private int mCurrentUploadProgressPercentage;

    /**
     * The broadcast action string to be used when broadcasting an Intent for any listening
     * BroadcastReceivers
     */
    public static final String BROADCAST_ACTION =
            "com.brettnamba.capsules.services.savecapsule.BROADCAST";

    /**
     * The name of the Service
     */
    private static final String SERVICE_NAME = "SaveCapsuleService";

    /**
     * The unique ID for notifications for this IntentService
     */
    private static final int NOTIFICATION_ID = 1;

    /**
     * Constructor
     */
    public SaveCapsuleService() {
        super(SaveCapsuleService.SERVICE_NAME);
    }

    /**
     * onCreate
     */
    @Override
    public void onCreate() {
        super.onCreate();
        // Get the WakeLock object
        PowerManager powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                SaveCapsuleService.SERVICE_NAME);
        // Get the NotificationManager
        this.mNotifyManager = (NotificationManager) this.getSystemService(
                Context.NOTIFICATION_SERVICE);
        // Instantiate the Notification builder
        this.mNotificationBuilder = new NotificationCompat.Builder(this);
        this.mNotificationBuilder.setContentTitle(this.getString(R.string.progress_saving_capsule))
                .setContentText(this.getString(R.string.progress_please_wait))
                .setSmallIcon(R.drawable.ic_place_black_24dp);
    }

    /**
     * onHandleIntent
     *
     * @param intent The Intent used to start this IntentService
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        // Make sure the Intent is not null
        if (intent == null) {
            return;
        }
        // Make sure the Intent has extras
        Bundle extras = intent.getExtras();
        if (extras == null) {
            return;
        }
        // Make sure the Intent extras has a bundled Capsule
        if (extras.containsKey("capsule")) {
            this.mCapsule = extras.getParcelable("capsule");
            if (this.mCapsule == null) {
                return;
            }
        }
        // Make sure the Intent extras has a bundled Account
        if (extras.containsKey("account")) {
            this.mAccount = extras.getParcelable("account");
            if (this.mAccount == null) {
                return;
            }
        }

        // Prevent the device from sleeping
        this.mWakeLock.acquire();

        // Execute a HTTP request to just validate the Capsule text data
        HttpUrlWwwFormRequest validationRequest = this.sendValidationRequest(this.mCapsule);
        if (validationRequest.isSuccess()) {
            // The validation was successful, so perform the real request
            this.sendCapsuleSaveRequest(this.mCapsule);
        } else {
            // The validation was not successful, so display the error messages
            this.parseAndBroadcastErrors(validationRequest);
        }

        // Release the WakeLock
        this.mWakeLock.release();
    }

    /**
     * Determines the total number of bytes uploaded and sends out a notification if the progress
     * has changed
     *
     * @param bytesUploaded The number of bytes that were just written to the stream
     * @param totalBytes    The total number of bytes for the whole HTTP request
     */
    @Override
    public void onDataSent(long bytesUploaded, long totalBytes) {
        this.mTotalBytesUploaded += bytesUploaded;
        int currentProgressPercentage = (int) (100 * this.mTotalBytesUploaded / totalBytes);
        if (currentProgressPercentage > this.mCurrentUploadProgressPercentage) {
            // Set the new progress status on the Notification
            this.mNotificationBuilder.setProgress(100, currentProgressPercentage, false);
            this.mNotifyManager.notify(NOTIFICATION_ID, this.mNotificationBuilder.build());
            // Retain the most recent progress percentage
            this.mCurrentUploadProgressPercentage = currentProgressPercentage;
        }
    }

    /**
     * Sends a Capsule validation HTTP request and returns the HTTP request object that also
     * contains the HTTP response data
     *
     * @param capsule The Capsule to validate
     * @return The HTTP request object that also contains the HTTP response data
     */
    private HttpUrlWwwFormRequest sendValidationRequest(Capsule capsule) {
        // Send the request
        return RequestHandler.validateCapsule(this, this.mAccount, capsule, this);
    }

    /**
     * Sends a Capsule save HTTP request and returns the HTTP request object that also contains
     * the HTTP response data
     *
     * @param capsule The Capsule to save
     */
    private void sendCapsuleSaveRequest(Capsule capsule) {
        // Send the save request
        HttpUrlMultiPartRequest httpRequest = RequestHandler.saveCapsule(this, this.mAccount,
                capsule, this);

        // Determine if the request was a success
        if (httpRequest.isSuccess()) {
            // Build a successful notification
            this.mNotificationBuilder.setContentText(this.getString(R.string.result_complete))
                    .setProgress(0, 0, false);
            // Broadcast the successful result
            this.parseAndBroadcastSuccess(httpRequest);
        } else {
            // Build an error notification
            this.mNotificationBuilder.setContentText(
                    this.getString(R.string.result_error_encountered)).setProgress(0, 0, false);
            // The save was not successful, so display the error messages
            this.parseAndBroadcastErrors(httpRequest);
        }
        // Send the notification
        this.mNotifyManager.notify(NOTIFICATION_ID, this.mNotificationBuilder.build());
    }

    /**
     * Parses a successful HTTP request object and broadcasts the result
     *
     * @param httpRequest The successful HTTP request object
     */
    private void parseAndBroadcastSuccess(HttpUrlConnectionRequest httpRequest) {
        // Will hold the newly saved Capsule parsed from the HTTP response
        Capsule savedCapsule = null;
        try {
            // Parse the HTTP response
            JSONObject jsonResponse = new JSONObject(httpRequest.getResponseBody());
            savedCapsule = JSONParser.parseOwnershipCapsule(jsonResponse);
        } catch (JSONException e) {
            // There was a parsing error, so broadcast an error
            List<String> messages = new ArrayList<String>();
            messages.add(this.getString(R.string.error_cannot_parse_http_response));
            this.broadcastError(messages);
        } finally {
            // Broadcast the newly saved Capsule
            this.broadcastSuccess(savedCapsule);
        }
    }

    /**
     * Parses an unsuccessful HTTP request object and broadcasts the result
     *
     * @param httpRequest The unsuccessful HTTP request object
     */
    private void parseAndBroadcastErrors(HttpUrlConnectionRequest httpRequest) {
        // Will hold any validation messages
        List<String> validationMessages = new ArrayList<String>();
        try {
            // Parse the HTTP response
            JSONObject jsonResponse = new JSONObject(httpRequest.getResponseBody());
            validationMessages = JSONParser.parseSaveCapsuleMessages(jsonResponse);
        } catch (JSONException e) {
            // There was a parse error, so indicate that in the error messages
            validationMessages.add(this.getString(R.string.error_cannot_parse_http_response));
        } finally {
            // Broadcast the error messages
            this.broadcastError(validationMessages);
        }
    }

    /**
     * Creates an Intent with the newly saved Capsule and broadcasts it
     *
     * @param capsule The newly saved Capsule
     */
    private void broadcastSuccess(Capsule capsule) {
        // Create an Intent to broadcast
        Intent intent = new Intent(BROADCAST_ACTION);
        intent.putExtra("capsule", capsule);
        // Broadcast the result
        this.sendBroadcast(intent);
    }

    /**
     * Creates an Intent with the specified messages and broadcasts it
     *
     * @param messages A collection of messages to broadcast
     */
    private void broadcastError(List<String> messages) {
        // Create an Intent to broadcast
        Intent intent = new Intent(BROADCAST_ACTION);
        intent.putStringArrayListExtra("messages", (ArrayList<String>) messages);
        // Broadcast the result
        this.sendBroadcast(intent);
    }

}
