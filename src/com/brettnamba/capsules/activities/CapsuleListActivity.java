package com.brettnamba.capsules.activities;

import android.accounts.Account;
import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.brettnamba.capsules.R;
import com.brettnamba.capsules.fragments.CapsuleListFragment;
import com.brettnamba.capsules.provider.CapsuleContract;
import com.brettnamba.capsules.util.Widgets;
import com.brettnamba.capsules.view.FragmentCollectionPagerAdapter;

/**
 * Activity that displays a user's Capsules and Discovery Capsules.
 *
 * @author Brett Namba
 */
public class CapsuleListActivity extends FragmentActivity implements
        CapsuleListFragment.CapsuleListFragmentListener {

    /**
     * onCreate
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_capsule_list);

        // The current Account
        Account account = null;

        // Get the Intent extras
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            account = extras.getParcelable("account");
        }

        // Close the Activity if the required data was not present in the extras
        if (account == null) {
            this.finish();
        }

        // Bundle data to be passed to the Capsule List Fragment
        Bundle ownershipBundle = new Bundle();
        ownershipBundle.putBoolean("owned", true);
        ownershipBundle.putParcelable("account", account);
        ownershipBundle.putParcelable("uri", CapsuleContract.Ownerships.CONTENT_URI.buildUpon()
                        .appendQueryParameter(CapsuleContract.Query.Parameters.INNER_JOIN, CapsuleContract.Capsules.TABLE_NAME)
                        .build()
        );
        ownershipBundle.putStringArray("projection", CapsuleContract.Ownerships.CAPSULE_JOIN_PROJECTION);
        ownershipBundle.putString("selection", CapsuleContract.Ownerships.ACCOUNT_NAME + " = ?");
        ownershipBundle.putStringArray("selection_args", new String[]{account.name});

        // Bundle the data to be passed to the Discovery List Fragment
        Bundle discoveryBundle = new Bundle();
        discoveryBundle.putBoolean("owned", false);
        discoveryBundle.putParcelable("account", account);
        discoveryBundle.putParcelable("uri", CapsuleContract.Discoveries.CONTENT_URI.buildUpon()
                        .appendQueryParameter(CapsuleContract.Query.Parameters.INNER_JOIN, CapsuleContract.Capsules.TABLE_NAME)
                        .build()
        );
        discoveryBundle.putStringArray("projection", CapsuleContract.Discoveries.CAPSULE_JOIN_PROJECTION);
        discoveryBundle.putString("selection", CapsuleContract.Discoveries.ACCOUNT_NAME + " = ?");
        discoveryBundle.putStringArray("selection_args", new String[]{account.name});

        // Instantiate the Fragments
        // Fragment that will hold the list of Ownerships
        CapsuleListFragment ownershipFragment = new CapsuleListFragment();
        ownershipFragment.setArguments(ownershipBundle);
        // Fragment that will hold the list of Discoveries
        CapsuleListFragment discoveryFragment = new CapsuleListFragment();
        discoveryFragment.setArguments(discoveryBundle);

        // Instantiate the PagerAdapter
        FragmentCollectionPagerAdapter pagerAdapter =
                new FragmentCollectionPagerAdapter(this.getSupportFragmentManager());
        // Set the Fragments on the pager
        pagerAdapter.addFragment(ownershipFragment)
                .addFragment(discoveryFragment);

        // Set up the tabs
        TabLayout tabLayout = (TabLayout) this.findViewById(R.id.activity_capsule_list_tabs);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.title_my_capsules));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.title_my_discoveries));

        // Get the ViewPager
        ViewPager viewPager = (ViewPager) this.findViewById(R.id.fragment_capsule_list_pager);
        // Set the PagerAdapter on the ViewPager
        viewPager.setAdapter(pagerAdapter);
        // Set the page change listener
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        // Add the OnTabSelectedListener implemented by the ViewPager
        tabLayout.setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));

        // Setup the Toolbar
        Toolbar toolbar = Widgets.createToolbar(this, this.getString(R.string.title_my_collection));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close this Activity
                CapsuleListActivity.this.finish();
            }
        });
    }

    /**
     * Callback for when a CapsuleListFragment is not passed the required data
     *
     * @param capsuleListFragment The Fragment that is missing the data
     */
    @Override
    public void onMissingData(CapsuleListFragment capsuleListFragment) {
        this.getSupportFragmentManager().beginTransaction().remove(capsuleListFragment).commit();
        this.setResult(Activity.RESULT_CANCELED);
        this.finish();
        Toast.makeText(this, this.getString(R.string.error_cannot_load_capsules),
                Toast.LENGTH_SHORT).show();
    }

}
