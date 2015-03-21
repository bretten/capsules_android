package com.brettnamba.capsules.widget;

/**
 * Represents an abstraction of a navigation drawer ListView item
 *
 * @author Brett Namba
 */
public abstract class NavigationDrawerItem implements ListViewItem {

    /**
     * The text to be displayed in the item
     */
    protected String mText;

    /**
     * Indicates if this drawer item belongs to a group of other ListView items
     */
    protected boolean mIsInGroup;

    /**
     * Constructor
     *
     * @param text
     */
    public NavigationDrawerItem(String text) {
        this.mText = text;
        this.mIsInGroup = false;
    }

    /**
     * Constructor
     *
     * @param text
     * @param isInGroup
     */
    public NavigationDrawerItem(String text, boolean isInGroup) {
        this.mText = text;
        this.mIsInGroup = isInGroup;
    }

    /**
     * Gets the text
     *
     * @return The text for this item
     */
    public String getText() {
        return this.mText;
    }

    /**
     * Sets the text
     *
     * @param text
     *
     * @return Returns self to allow chaining
     */
    public NavigationDrawerItem setText(String text) {
        this.mText = text;
        return this;
    }

    /**
     * Checks if this item belongs to a group
     *
     * @return Whether or not the item is in a group
     */
    public boolean isInGroup() {
        return this.mIsInGroup;
    }

    /**
     * Sets whether or not the item is in a group
     *
     * @param isInGroup
     * @return Returns self to allow chaining
     */
    public NavigationDrawerItem setIsInGroup(boolean isInGroup) {
        this.mIsInGroup = isInGroup;
        return this;
    }

}
