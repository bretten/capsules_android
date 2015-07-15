package com.brettnamba.capsules.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.File;
import java.util.UUID;

/**
 * Utility class for work related to files
 *
 * @author Brett Namba
 */
public final class Files {

    /**
     * Private constructor to prevent instantiation
     */
    private Files() {
    }

    /**
     * Generates a unique file name
     *
     * @return A unique file name
     */
    public static String getNewUniqueFileName() {
        return UUID.randomUUID().toString();
    }

    /**
     * Generates a unique file name for the device's camera by appending the JPEG file extension
     * since Android currently only takes JPEGs.
     *
     * @return A unique file name for an image taken by the camera
     */
    public static String getNewUniqueFileNameForCamera() {
        return Files.getNewUniqueFileName() + ".jpg";
    }

    /**
     * Gets the file size of the file at the specified URI.  The URI can be a file or content URI
     * and the size will be determined accordingly.
     *
     * @param context The current Context so the ContentResolver can be accessed
     * @param uri     The URI of the file which can be a content or file URI
     * @return The size of the file in bytes
     */
    public static long getFileSize(Context context, Uri uri) {
        // Check the scheme
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            // The URI is a content URI, so the file size can be queried using a ContentProvider
            Cursor c = context.getContentResolver()
                    .query(uri, new String[]{OpenableColumns.SIZE}, null, null, null);
            // Get the column index of the file size column
            int sizeIndex = c.getColumnIndex(OpenableColumns.SIZE);
            // Move to the first matching row
            if (c.moveToFirst()) {
                // Get the size
                long size = c.getLong(sizeIndex);
                // Close the cursor
                c.close();
                return size;
            } else {
                // No matching row was found
                return 0;
            }
        } else {
            // The URI is a file path
            File file = new File(uri.getPath());

            return file.length();
        }
    }

}
