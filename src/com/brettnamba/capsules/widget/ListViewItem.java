package com.brettnamba.capsules.widget;

/**
 * Generalized interface for representing an item within a ListView
 *
 * @author Brett Namba
 */
public interface ListViewItem {

    /**
     * Specifies if the ListViewItem is a header or not
     *
     * @return True indicates it is a header, false indicates it is not
     */
    boolean isHeader();

    /**
     * Specifies if the ListViewItem belongs to a group of other ListViewItems
     *
     * @return True indicates it belongs to a group, false indicates it does not
     */
    boolean isInGroup();

}
