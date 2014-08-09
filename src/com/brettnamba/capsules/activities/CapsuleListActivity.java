package com.brettnamba.capsules.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;

import com.brettnamba.capsules.R;
import com.brettnamba.capsules.fragments.CapsuleListFragment;

/**
 * Activity that displays a user's Capsules and Discovery Capsules.
 * 
 * @author Brett Namba
 *
 */
public class CapsuleListActivity extends ActionBarActivity {

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

        // Bundle data to be passed to the Fragments
        Bundle capsuleListBundle = new Bundle();
        capsuleListBundle.putString("account_name", accountName);

        // Add any Fragments
        CapsuleListFragment capsuleListFragment = new CapsuleListFragment();
        capsuleListFragment.setArguments(capsuleListBundle);
        if (savedInstanceState == null) {
            FragmentManager fragmentManager = this.getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.fragment_capsule_list, capsuleListFragment);
            fragmentTransaction.commit();
        }
    }

}
