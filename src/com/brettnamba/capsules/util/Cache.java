package com.brettnamba.capsules.util;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

/**
 * Utility class for handling caching related tasks
 */
public final class Cache {

    /**
     * Private constructor to prevent instantiation
     */
    private Cache() {
    }

    /**
     * Gets a LruCache instance for caching Bitmaps by String keys
     *
     * @return A new instance of a LruCache for caching Bitmaps with String keys
     */
    public static LruCache<String, Bitmap> getBitmapLruCacheInstance() {
        // Get the max memory
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        // Use only a portion of the max available memory for caching
        final int cacheSize = maxMemory / 8;

        return new LruCache<String, Bitmap>(cacheSize) {

            /**
             * Returns the byte size of the Bitmap
             *
             * @param key The key of the cached Bitmap
             * @param value The cached Bitmap
             * @return The byte size of the Bitmap
             */
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return (value.getRowBytes() * value.getHeight()) / 1024;
            }

        };
    }

}
