package com.brettnamba.capsules.view;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

/**
 * PagerAdapter implementation that provides an easy way to add Fragments.  Stores Fragments
 * in a collection so they can easily be retrieved by position with getItem() and also so
 * the count is determined by the collection's method.
 *
 * NOTE: FragmentPagerAdapter keeps track of all the Fragments in its FragmentManager
 *
 * @author Brett Namba
 */
public class FragmentCollectionPagerAdapter extends FragmentPagerAdapter {

    /**
     * The collection of Fragments
     */
    private ArrayList<Fragment> mFragments;

    /**
     * Constructor
     *
     * @param fm The FragmentManager
     */
    public FragmentCollectionPagerAdapter(FragmentManager fm) {
        super(fm);
        this.mFragments = new ArrayList<Fragment>();
    }

    /**
     * Gets the item at the specified position in the Fragment collection
     *
     * @param position The position in the Fragment collection
     * @return Fragment if it is found, otherwise null
     */
    @Override
    public Fragment getItem(int position) {
        if (this.mFragments != null) {
            return this.mFragments.get(position);
        }
        return null;
    }

    /**
     * Returns the number of Fragments in the collection
     *
     * @return The number of Fragments in the collection, or 0 if it is empty
     */
    @Override
    public int getCount() {
        if (this.mFragments != null) {
            return this.mFragments.size();
        }
        return 0;
    }

    /**
     * Adds a Fragment to the collection.  Allows chaining by returning the current instance
     *
     * @param fragment The Fragment to add to the collection
     * @return Reference to self to allow chaining
     */
    public FragmentCollectionPagerAdapter addFragment(Fragment fragment) {
        // Instantiate the collection if it is null
        if (this.mFragments == null) {
            this.mFragments = new ArrayList<Fragment>();
        }
        // Add the Fragment to the collection
        this.mFragments.add(fragment);
        return this;
    }

}
