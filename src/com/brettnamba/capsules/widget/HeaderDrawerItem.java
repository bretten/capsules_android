package com.brettnamba.capsules.widget;

/**
 * Represents a header item in a navigation drawer
 *
 * @author Brett Namba
 */
public class HeaderDrawerItem extends NavigationDrawerItem {

    /**
     * Constructor
     *
     * @param text
     */
    public HeaderDrawerItem(String text) {
        super(text);
    }

    /**
     * This is a header, so return true
     *
     * @return true
     */
    public boolean isHeader() {
        return true;
    }

}
