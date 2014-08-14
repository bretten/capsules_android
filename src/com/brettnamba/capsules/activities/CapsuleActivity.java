package com.brettnamba.capsules.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.brettnamba.capsules.R;
import com.brettnamba.capsules.fragments.CapsuleContentFragment;
import com.brettnamba.capsules.fragments.CapsuleFragment;
import com.brettnamba.capsules.fragments.DiscoveryFragment;

/**
 * The Activity that shows information about a Capsule.
 * 
 * @author Brett Namba
 *
 */
public class CapsuleActivity extends ActionBarActivity {

    /**
     * Whether or not this Capsule is owned by the current Account.
     */
    private boolean mOwned = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capsule);

        // Get the Intent extras
        Bundle extras = getIntent().getExtras();
        long capsuleId = 0;
        String accountName = null;
        if (extras != null) {
            mOwned = extras.getBoolean("owned");
            capsuleId = extras.getLong("capsule_id");
            accountName = extras.getString("account_name");
        }

        // Bundle the Fragment arguments
        Bundle bundle = new Bundle();
        bundle.putLong("capsule_id", capsuleId);
        bundle.putString("account_name", accountName);

        // Add any Fragments
        Fragment capsuleFragment = new CapsuleFragment();
        capsuleFragment.setArguments(bundle);
        Fragment discoveryFragment = null;
        if (!mOwned) {
            discoveryFragment = new DiscoveryFragment();
            discoveryFragment.setArguments(bundle);
        }
        if (savedInstanceState == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.fragment_capsule, capsuleFragment);
            if (!mOwned) {
                fragmentTransaction.add(R.id.fragment_discovery, discoveryFragment);
            }
            fragmentTransaction.add(R.id.fragment_capsule_content, new CapsuleContentFragment());
            fragmentTransaction.commit();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.capsule, menu);
        // Show the Edit button if this is an Ownership
        if (mOwned) {
            MenuItem editItem = (MenuItem) menu.findItem(R.id.action_edit);
            editItem.setVisible(true);
        }
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
