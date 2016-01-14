package com.brettnamba.capsules.util;

import android.app.Activity;
import android.support.v7.widget.Toolbar;

import com.brettnamba.capsules.R;

/**
 * Utility class that contains common View widget methods
 */
public final class Widgets {

    /**
     * Private constructor to prevent instantiating
     */
    private Widgets() {
    }

    /**
     * Instantiates the standard Toolbar View widget for the app
     *
     * @param activity   The Activity to place the Toolbar in
     * @param subtitle   The subtitle text
     * @param isTopLevel Determines if the hosting Activity is a top-level Activity
     * @return The Toolbar
     */
    public static Toolbar createToolbar(Activity activity, String subtitle, boolean isTopLevel) {
        Toolbar toolbar = (Toolbar) activity.findViewById(R.id.toolbar);
        toolbar.setTitle(activity.getString(R.string.app_name));
        toolbar.setSubtitle(subtitle);
        if (isTopLevel) {
            toolbar.setNavigationIcon(R.drawable.ic_menu_grey);
        } else {
            toolbar.setNavigationIcon(R.drawable.ic_chevron_left_white);
        }
        toolbar.setLogo(R.drawable.ic_launcher);
        return toolbar;
    }

}
