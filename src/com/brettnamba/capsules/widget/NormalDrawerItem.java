package com.brettnamba.capsules.widget;

import android.graphics.drawable.Drawable;

/**
 * Represents a standard item in a navigation drawer
 *
 * @author Brett Namba
 */
public class NormalDrawerItem extends NavigationDrawerItem {

    /**
     * The Drawable icon to be displayed along with this item
     */
    private Drawable mDrawable;

    /**
     * Constructor
     *
     * @param text
     */
    public NormalDrawerItem(String text) {
        super(text);
    }

    /**
     * Constructor
     *
     * @param text
     * @param isInGroup
     */
    public NormalDrawerItem(String text, boolean isInGroup) {
        super(text, isInGroup);
    }

    /**
     * This is a standard item, so return false
     *
     * @return false
     */
    public boolean isHeader() {
        return false;
    }

    /**
     * Gets the Drawable
     *
     * @return The Drawable icon for this item
     */
    public Drawable getDrawable() {
        return this.mDrawable;
    }

    /**
     * Sets the Drawable
     *
     * @param drawable
     * @return Returns self to allow chaining
     */
    public NavigationDrawerItem setDrawable(Drawable drawable) {
        this.mDrawable = drawable;
        return this;
    }

}
