package com.brettnamba.capsules.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;

import com.brettnamba.capsules.R;
import com.brettnamba.capsules.fragments.CapsuleEditorFragment;

/**
 * This Activity is responsible for presenting the user with a UI to edit a Capsule and its associated data.
 * 
 * @author Brett Namba
 *
 */
public class CapsuleEditorActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capsule_editor);

        // Get Intent extras
        Bundle extras = getIntent().getExtras();
        double latitude = 0;
        double longitude = 0;
        String accountName = null;
        if (extras != null) {
            latitude = extras.getDouble("latitude");
            longitude = extras.getDouble("longitude");
            accountName = extras.getString("account_name");
        }

        // Bundle data for Fragments
        Bundle capsuleEditorBundle = new Bundle();
        capsuleEditorBundle.putDouble("latitude", latitude);
        capsuleEditorBundle.putDouble("longitude", longitude);
        capsuleEditorBundle.putString("account_name", accountName);

        // Add any Fragments
        CapsuleEditorFragment capsuleEditorFragment = new CapsuleEditorFragment();
        capsuleEditorFragment.setArguments(capsuleEditorBundle);
        if (savedInstanceState == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.fragment_capsule_editor, capsuleEditorFragment);
            fragmentTransaction.commit();
        }
    }

}
