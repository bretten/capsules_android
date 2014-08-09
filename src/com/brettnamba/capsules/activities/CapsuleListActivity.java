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

        // Add any Fragments
        if (savedInstanceState == null) {
            FragmentManager fragmentManager = this.getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.fragment_capsule_list, new CapsuleListFragment());
            fragmentTransaction.commit();
        }
    }

}
