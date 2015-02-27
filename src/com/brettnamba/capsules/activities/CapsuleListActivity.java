package com.brettnamba.capsules.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;

import com.brettnamba.capsules.R;
import com.brettnamba.capsules.fragments.CapsuleListFragment;
import com.brettnamba.capsules.provider.CapsuleContract;

/**
 * Activity that displays a user's Capsules and Discovery Capsules.
 * 
 * @author Brett Namba
 *
 */
public class CapsuleListActivity extends ActionBarActivity {

    /**
     * The Fragment tag for the Ownerships CapsuleListFragment
     */
    private static final String FRAGMENT_TAG_OWNERSHIP_LIST = "ownerships";

    /**
     * The Fragment tag for the Discoveries CapsuleListFragment
     */
    private static final String FRAGMENT_TAG_DISCOVERY_LIST = "discoveries";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capsule_list);

        // Get Intent extras
        Bundle extras = getIntent().getExtras();
        String accountName = null;
        if (extras != null) {
            accountName = extras.getString("account_name");
        }

        // Bundle data to be passed to the Capsule List Fragment
        Bundle ownershipBundle = new Bundle();
        ownershipBundle.putBoolean("owned", true);
        ownershipBundle.putString("account_name", accountName);
        ownershipBundle.putParcelable("uri", CapsuleContract.Ownerships.CONTENT_URI.buildUpon()
                .appendQueryParameter(CapsuleContract.QUERY_PARAM_JOIN, CapsuleContract.Capsules.TABLE_NAME)
                .build()
        );
        ownershipBundle.putStringArray("projection", new String[]{CapsuleContract.Capsules.NAME, CapsuleContract.Ownerships.ACCOUNT_NAME});
        ownershipBundle.putString("selection", CapsuleContract.Ownerships.ACCOUNT_NAME + " = ?");
        ownershipBundle.putStringArray("selection_args", new String[]{accountName});

        // Bundle the data to be passed to the Discovery List Fragment
        Bundle discoveryBundle = new Bundle();
        discoveryBundle.putBoolean("owned", false);
        discoveryBundle.putString("account_name", accountName);
        discoveryBundle.putParcelable("uri", CapsuleContract.Discoveries.CONTENT_URI.buildUpon()
                .appendQueryParameter(CapsuleContract.QUERY_PARAM_JOIN, CapsuleContract.Capsules.TABLE_NAME)
                .build()
        );
        discoveryBundle.putStringArray("projection", new String[]{CapsuleContract.Capsules.NAME, CapsuleContract.Discoveries.ACCOUNT_NAME});
        discoveryBundle.putString("selection", CapsuleContract.Discoveries.ACCOUNT_NAME + " = ?");
        discoveryBundle.putStringArray("selection_args", new String[]{accountName});

        // Set up the ActionBar Tabs
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.addTab(actionBar.newTab()
                .setText(getString(R.string.tab_ownerships))
                .setTabListener(new TabListener<CapsuleListFragment>(this, FRAGMENT_TAG_OWNERSHIP_LIST, CapsuleListFragment.class, ownershipBundle))
        );
        actionBar.addTab(actionBar.newTab()
                .setText(getString(R.string.tab_discoveries))
                .setTabListener(new TabListener<CapsuleListFragment>(this, FRAGMENT_TAG_DISCOVERY_LIST, CapsuleListFragment.class, discoveryBundle))
        );
    }

    @Override
    public void onBackPressed() {
        // Get the OwnershipList Fragment
        CapsuleListFragment ownershipList = (CapsuleListFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_OWNERSHIP_LIST);
        Intent intent = new Intent();
        intent.putExtra("modified", ownershipList.wasModified());
        setResult(Activity.RESULT_OK, intent);
        super.onBackPressed();
    }

    /**
     * TabListener for switching between CapsuleListFragments.
     * 
     * @param <T>
     */
    public static class TabListener<T extends Fragment> implements ActionBar.TabListener {

        /**
         * The parent Activity
         */
        private final ActionBarActivity activity;

        /**
         * The Tab tag
         */
        private final String tag;

        /**
         * The Fragment class
         */
        private final Class<T> clazz;

        /**
         * Data Bundle for the Fragment
         */
        private final Bundle bundle;

        /**
         * Reference to the Fragment
         */
        private Fragment fragment;

        /**
         * Constructor
         * 
         * @param activity
         * @param tag
         * @param clazz
         */
        public TabListener(Activity activity, String tag, Class<T> clazz) {
            this(activity, tag, clazz, null);
        }

        /**
         * Constructor with a data Bundle parameter
         * 
         * @param activity
         * @param tag
         * @param clazz
         * @param bundle
         */
        public TabListener(Activity activity, String tag, Class<T> clazz, Bundle bundle) {
            this.activity = (ActionBarActivity) activity;
            this.tag = tag;
            this.clazz = clazz;
            this.bundle = bundle;

            this.fragment = this.activity.getSupportFragmentManager().findFragmentByTag(this.tag);
            if (this.fragment != null && !this.fragment.isDetached()) {
                FragmentTransaction ft = this.activity.getSupportFragmentManager().beginTransaction();
                ft.detach(this.fragment).commit();
            }
        }

        @Override
        public void onTabReselected(Tab tab, FragmentTransaction ft) {}

        @Override
        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            if (this.fragment == null) {
                this.fragment = Fragment.instantiate(this.activity, this.clazz.getName(), this.bundle);
                ft.add(R.id.fragment_capsule_list, this.fragment, this.tag);
            } else {
                ft.attach(this.fragment);
            }
        }

        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
            if (this.fragment != null) {
                ft.detach(this.fragment);
            }
        }

    }

}
