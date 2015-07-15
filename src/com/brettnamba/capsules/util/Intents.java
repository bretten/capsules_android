package com.brettnamba.capsules.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.util.Pair;

import com.brettnamba.capsules.R;

import java.io.File;

/**
 * Utility class for work related to Intents
 *
 * @author Brett Namba
 */
public final class Intents {

    /**
     * Private constructor to prevent instantiation
     */
    private Intents() {
    }

    /**
     * Creates an Intent chooser with a gallery Intent and, if available, a camera Intent.  If the
     * camera Intent is available, a placeholder file will be created to store the image taken from
     * the camera.  This method will then return the chooser Intent along with the URI of the image
     * taken from the camera.  If no image was taken, the URI will be null.
     *
     * @param context The current Context used to access the PackageManager and resources
     * @return An object containing both the chooser Intent and the URI of the image taken from the
     * camera. If no image was taken then the URI will be null.
     */
    public static Pair<Intent, Uri> getCameraAndGalleryIntentChooser(Context context) {
        // The Pair that will contain the chooser Intent and the URI of the camera file if any
        Pair<Intent, Uri> intentUriPair;
        // Gallery Intent
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        // Intent to prompt the user how they want to get the file
        Intent chooseIntent = Intent.createChooser(galleryIntent,
                context.getString(R.string.title_upload_source_chooser));
        // Camera Intent
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Make sure the camera Intent can be used
        if (cameraIntent.resolveActivity(context.getPackageManager()) != null) {
            final File directory = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES);
            final String filename = Files.getNewUniqueFileNameForCamera();
            File image = new File(directory, filename);
            // Store a reference to the image URI
            Uri outUri = Uri.fromFile(image);
            // Add the output location to the camera Intent
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outUri);
            // Create a collection for the camera Intent
            Intent[] cameraIntents = new Intent[]{cameraIntent};
            // Add the camera Intent to the Intent chooser
            chooseIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents);
            // Add the Intent and URI to the Pair
            intentUriPair = new Pair<Intent, Uri>(chooseIntent, outUri);
        } else {
            // Add the Intent to the Pair
            intentUriPair = new Pair<Intent, Uri>(chooseIntent, null);
        }
        return intentUriPair;
    }

    /**
     * Determines if the Intent data from onActivityResult(requestCode, resultCode, data) is from
     * the device's camera Activity.
     *
     * @param data The Intent data from onActivityResult(requestCode, resultCode, data)
     * @return True if the Intent is from the camera Activity, otherwise false
     */
    public static boolean isActivityResultIntentFromCamera(Intent data) {
        final boolean isImageCaptureAction = data != null && data.getAction() != null
                && data.getAction().equals(MediaStore.ACTION_IMAGE_CAPTURE);
        final boolean isIntentNull = data == null;
        return isImageCaptureAction || isIntentNull;
    }

    /**
     * Notifies the Media Provider that a new image has been taken
     *
     * @param context The current Context so the broadcast can be sent
     * @param uri     The URI of the image that was taken
     */
    public static void notifyMediaProviderOfNewImage(Context context, Uri uri) {
        Intent mediaIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaIntent.setData(uri);
        context.sendBroadcast(mediaIntent);
    }

}
