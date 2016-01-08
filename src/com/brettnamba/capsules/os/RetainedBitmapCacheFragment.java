package com.brettnamba.capsules.os;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.LruCache;

import com.brettnamba.capsules.util.Cache;

/**
 * Fragment that is used to retain a cache of Bitmaps.  The state is retained by setting
 * setRetainInstance(true).
 */
public class RetainedBitmapCacheFragment extends Fragment {

    /**
     * The cache that is storing Bitmaps
     */
    private LruCache<String, Bitmap> mCache;

    /**
     * Tag to be used with the FragmentManager
     */
    private static final String TAG = "retained_bitmap_cache_fragment";

    /**
     * Sets the instance to be retained through the parent Activity's lifecycle
     *
     * @param savedInstanceState The instance state data
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Flag the Fragment to be retained
        this.setRetainInstance(true);
    }

    /**
     * Sets the cache
     *
     * @param cache The cache
     */
    public void setCache(LruCache<String, Bitmap> cache) {
        this.mCache = cache;
    }

    /**
     * Gets the cache or if none present, instantiates one
     *
     * @return The cache
     */
    public LruCache<String, Bitmap> getCache() {
        if (this.mCache == null) {
            this.mCache = Cache.getBitmapLruCacheInstance();
        }
        return this.mCache;
    }

    /**
     * Using the specified FragmentManager, will find the already added instance of
     * RetainedBitmapCacheFragment by its tag.  If it does not exist, will instantiate it and
     * add it to the FragmentManager.
     *
     * @param fm The FragmentManager
     * @return The RetainedBitmapCacheFragment found in the FragmentManager or a new instance
     */
    public static RetainedBitmapCacheFragment findOrCreate(FragmentManager fm) {
        RetainedBitmapCacheFragment fragment =
                (RetainedBitmapCacheFragment) fm.findFragmentByTag(TAG);
        if (fragment == null) {
            fragment = new RetainedBitmapCacheFragment();
            fm.beginTransaction().add(fragment, TAG).commit();
        }

        return fragment;
    }

}
