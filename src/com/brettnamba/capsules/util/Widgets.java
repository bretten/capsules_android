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
     * @param activity The Activity to place the Toolbar in
     * @return The Toolbar
     */
    public static Toolbar createToolbar(Activity activity) {
        Toolbar toolbar = (Toolbar) activity.findViewById(R.id.toolbar);
        toolbar.setTitle(activity.getString(R.string.app_name));
        toolbar.setSubtitle(activity.getString(R.string.title_login));
        toolbar.setNavigationIcon(R.drawable.ic_chevron_left_white);
        toolbar.setLogo(R.drawable.ic_launcher);
        return toolbar;
    }

}
