package com.brettnamba.capsules.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Utility class for handling images
 *
 * @author Brett Namba
 */
public final class Images {

    /**
     * Private constructor to prevent instantiation
     */
    private Images() {
    }

    /**
     * The device width limit
     */
    private static final int DEVICE_WIDTH_LIMIT = 2048;

    /**
     * The device height limit
     */
    private static final int DEVICE_HEIGHT_LIMIT = 2048;

    /**
     * Gets a Bitmap from a file or content URI by reading the stream
     *
     * @param context The current Context to get the ContentResolver
     * @param uri     The file or content URI of the image
     * @return A Bitmap of the image located at the URI
     * @throws FileNotFoundException
     */
    public static Bitmap getImageFromUri(Context context, Uri uri) throws FileNotFoundException {
        // Open a stream
        InputStream in = context.getContentResolver().openInputStream(uri);
        // Decode the bitmap
        return BitmapFactory.decodeStream(in);
    }

    /**
     * Scales a Bitmap by the specified scale factor and ensures that it does not exceed the
     * dimensions of the screen
     *
     * @param context          The current Context to get the WindowManager
     * @param bitmap           The Bitmap to be scaled
     * @param widthScaleFactor The factor to scale the width by
     * @return The scaled Bitmap
     */
    public static Bitmap scaleBitmap(Context context, Bitmap bitmap, double widthScaleFactor) {
        // Get the current screen dimensions
        int screenWidth;
        int screenHeight;
        // Get the current Display and to get the dimensions
        WindowManager windowManager = (WindowManager) context.getSystemService(
                Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Point point = new Point();
            display.getSize(point);
            screenWidth = point.x;
            screenHeight = point.y;
        } else {
            screenWidth = display.getWidth();
            screenHeight = display.getHeight();
        }

        // Determine the max width and heights
        int maxWidth;
        int maxHeight;
        // See if the Display dimensions can be used or if they exceed the device's limits
        if (screenWidth > Images.DEVICE_WIDTH_LIMIT) {
            maxWidth = Images.DEVICE_WIDTH_LIMIT;
        } else {
            maxWidth = screenWidth;
        }
        if (screenHeight > Images.DEVICE_HEIGHT_LIMIT) {
            maxHeight = Images.DEVICE_HEIGHT_LIMIT;
        } else {
            maxHeight = screenHeight;
        }

        // Scale the dimensions based on the width scale factor
        int scaledWidth = (int) Math.round(widthScaleFactor * maxWidth);
        // Scale the height to maintain the aspect ratio
        int scaledHeight = (scaledWidth * bitmap.getHeight()) / bitmap.getWidth();

        // See if the width exceeds the max width
        if (scaledWidth > maxWidth) {
            // Fit the width to the max width
            scaledWidth = maxWidth;
            // Scale the height to maintain the aspect ratio
            scaledHeight = (scaledWidth * bitmap.getHeight()) / bitmap.getWidth();
        }
        // See if the height exceeds the max height
        if (scaledHeight > maxHeight) {
            // Fit the height to the max height
            scaledHeight = maxHeight;
            // Scale the width to maintain the aspect ratio
            scaledWidth = (scaledHeight * bitmap.getWidth()) / bitmap.getHeight();
        }

        return Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);
    }

}
