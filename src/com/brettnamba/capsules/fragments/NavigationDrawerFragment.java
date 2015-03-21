package com.brettnamba.capsules.fragments;

import android.accounts.Account;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brettnamba.capsules.R;
import com.brettnamba.capsules.widget.HeaderDrawerItem;
import com.brettnamba.capsules.widget.NavigationDrawerItem;
import com.brettnamba.capsules.widget.NavigationItemArrayAdapter;
import com.brettnamba.capsules.widget.NormalDrawerItem;

import java.util.ArrayList;

/**
 * This Fragment is the main navigation drawer for the application
 *
 * @author Brett Namba
 */
public class NavigationDrawerFragment extends Fragment {

    /**
     * The View containing this Fragment
     */
    private View mFragmentView;

    /**
     * The DrawerLayout View
     */
    private DrawerLayout mDrawerLayout;

    /**
     * Collection of items that will be listed in the drawer
     */
    private ArrayList<NavigationDrawerItem> mNavItems;

    /**
     * The ListView holds the navigation items
     */
    private ListView mListView;

    /**
     * The TextView that displays the Account
     */
    private TextView mAccountTextView;

    /**
     * onCreateView
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout
        RelativeLayout view = (RelativeLayout) inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        // Find the View that holds the Account name
        this.mAccountTextView = (TextView) view.findViewById(R.id.navigation_drawer_account);
        // Find the ListView
        this.mListView = (ListView) view.findViewById(R.id.navigation_drawer_list);
        // Set the click listener for the ListView
        this.mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Delegate to Fragment's method
                NavigationDrawerFragment.this.clickItem(position);
            }
        });
        // Add the ListView items
        this.mNavItems = this.createNavigationItemCollection();
        // Set the ListView Adapter
        this.mListView.setAdapter(new NavigationItemArrayAdapter(this.getActivity().getApplicationContext(), R.layout.navigation_drawer_normal_item, this.mNavItems));
        return view;
    }

    /**
     * Sets up the Fragment
     *
     * @param view
     * @param drawerLayout
     */
    public void initialize(View view, DrawerLayout drawerLayout, Account account) {
        this.mFragmentView = view;
        this.mDrawerLayout = drawerLayout;

        // Set the current Account in the corresponding TextView
        this.switchAccount(account);
    }

    /**
     * Builds the collection of navigation items
     *
     * Currently this uses Resource arrays to match up the drawable icons and the strings.  The
     * arrays are separate but the drawable icon and its corresponding string are in the same
     * positions
     *
     * @return The collection of navigation items
     */
    private ArrayList<NavigationDrawerItem> createNavigationItemCollection() {
        Resources resources = this.getResources();
        // Get the image drawables that will be used for each item
        // NOTE: The positions in the array need to be matched to the corresponding strings
        TypedArray toggleIcons = resources.obtainTypedArray(R.array.navigation_drawer_toggle_icons);
        TypedArray linkIcons = resources.obtainTypedArray(R.array.navigation_drawer_link_icons);

        // Initialize the collection
        ArrayList<NavigationDrawerItem> items = new ArrayList<NavigationDrawerItem>();
        // Add the toggle header item to the collection
        items.add(new HeaderDrawerItem(resources.getString(R.string.navigation_drawer_header_toggles)));
        // Add the toggle items to the collection
        String[] toggles = resources.getStringArray(R.array.navigation_drawer_toggles);
        for (int i = 0; i < toggles.length; i++) {
            items.add(new NormalDrawerItem(toggles[i], /* isInGroup */ true).setDrawable(toggleIcons.getDrawable(i)));
        }
        // Add the link header item to the collection
        items.add(new HeaderDrawerItem(resources.getString(R.string.navigation_drawer_header_links)));
        // Add the link items to the collection
        String[] links = resources.getStringArray(R.array.navigation_drawer_links);
        for (int i = 0; i < links.length; i++) {
            items.add(new NormalDrawerItem(links[i]).setDrawable(linkIcons.getDrawable(i)));
        }

        // Recycle the TypedArrays
        toggleIcons.recycle();
        linkIcons.recycle();

        return items;
    }

    /**
     * Delegate method for handling clicks on the navigation items
     *
     * @param position
     */
    private void clickItem(int position) {
        if (this.mNavItems != null) {
            // Check if the selected item is in a group
            if (this.mNavItems.get(position).isInGroup()) {
                // See if the item is checked
                if (this.mListView.isItemChecked(position)) {
                    // Perform the on-check action
                    // TODO: On-check action
                } else {
                    // Perform the un-check action
                    // TODO: Un-check action
                }
            } else {
                // Prevent items in a group from remaining checked
                this.mListView.setItemChecked(position, false);
                // Perform the action
                // TODO: Perform the action
            }
        }

        // Close the drawer
        if (this.mDrawerLayout != null) {
            this.mDrawerLayout.closeDrawer(this.mFragmentView);
        }
    }

    /**
     * Switches the active account for this Fragment
     *
     * @param account
     */
    private void switchAccount(Account account) {
        if (this.mAccountTextView != null) {
            this.mAccountTextView.setText(account.name);
        }
    }

}
