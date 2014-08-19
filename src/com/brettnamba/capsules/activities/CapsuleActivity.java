package com.brettnamba.capsules.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.brettnamba.capsules.R;
import com.brettnamba.capsules.dataaccess.Capsule;
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

    /**
     * The Capsule for this Activity
     */
    private Capsule mCapsule;

    /**
     * The Account name
     */
    private String mAccountName = null;

    /**
     * Whether or not the Capsule was modified
     */
    private boolean mModified = false;

    /**
     * Request code for starting the CapsuleEditorActivity
     */
    private static final int REQUEST_CODE_CAPSULE_EDITOR = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capsule);

        // Get the Intent extras
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mOwned = extras.getBoolean("owned");
            mCapsule = (Capsule) extras.getParcelable("capsule");
            mAccountName = extras.getString("account_name");
        }

        // Bundle the Fragment arguments
        Bundle bundle = new Bundle();
        bundle.putParcelable("capsule", mCapsule);
        bundle.putString("account_name", mAccountName);

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
        } else if (id == R.id.action_edit) {
            Intent intent = new Intent(getApplicationContext(), CapsuleEditorActivity.class);
            intent.putExtra("capsule", mCapsule);
            intent.putExtra("account_name", mAccountName);
            startActivityForResult(intent, REQUEST_CODE_CAPSULE_EDITOR);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("capsule", mCapsule);
        intent.putExtra("modified", mModified);
        setResult(Activity.RESULT_OK, intent);
        super.onBackPressed();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

        case (REQUEST_CODE_CAPSULE_EDITOR) :
            if (resultCode == Activity.RESULT_OK) {
                // Flag it as modified
                mModified = true;
                // Get the new Capsule
                mCapsule = (Capsule) data.getParcelableExtra("capsule");
                // Get the Fragment containing the Capsule information
                Fragment capsuleFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_capsule);
                // Populate the information
                TextView name = (TextView) capsuleFragment.getView().findViewById(R.id.fragment_capsule_info_name);
                name.setText(mCapsule.getName());
            }
            break;

        default:
            break;

        }

    }

}
