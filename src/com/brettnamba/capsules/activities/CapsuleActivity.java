package com.brettnamba.capsules.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.brettnamba.capsules.R;
import com.brettnamba.capsules.fragments.CapsuleContentFragment;
import com.brettnamba.capsules.fragments.CapsuleFragment;

/**
 * The Activity that shows information about a Capsule.
 * 
 * @author Brett Namba
 *
 */
public class CapsuleActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capsule);

        // Get the Intent extras
        Bundle extras = getIntent().getExtras();
        long capsuleId = 0;
        if (extras != null) {
            capsuleId = extras.getLong("capsule_id");
        }

        // Bundle the Fragment arguments
        Bundle capsuleFragmentBundle = new Bundle();
        capsuleFragmentBundle.putLong("capsule_id", capsuleId);

        // Add any Fragments
        Fragment capsuleFragment = new CapsuleFragment();
        capsuleFragment.setArguments(capsuleFragmentBundle);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, capsuleFragment).commit();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new CapsuleContentFragment()).commit();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.capsule, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
