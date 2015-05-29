package com.brettnamba.capsules.activities;

import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.brettnamba.capsules.R;
import com.brettnamba.capsules.dataaccess.Capsule;
import com.brettnamba.capsules.dataaccess.CapsuleOwnership;
import com.brettnamba.capsules.fragments.CapsuleContentFragment;
import com.brettnamba.capsules.fragments.CapsuleFragment;
import com.brettnamba.capsules.fragments.DiscoveryFragment;
import com.brettnamba.capsules.util.Widgets;

/**
 * Activity that displays a Capsule and other Fragments depending on the type of the Capsule
 *
 * @author Brett Namba
 */
public class CapsuleActivity extends FragmentActivity implements
        CapsuleFragment.CapsuleFragmentListener,
        DiscoveryFragment.DiscoveryFragmentListener {

    /**
     * Whether or not this Capsule is owned by the current Account.
     */
    private boolean mOwned = false;

    /**
     * The Capsule for this Activity
     */
    private Capsule mCapsule;

    /**
     * The Account that is opening this Activity
     */
    private Account mAccount;

    /**
     * Whether or not the Capsule was modified
     */
    private boolean mModified = false;

    /**
     * The Activity's Toolbar widget
     */
    private Toolbar mToolbar;

    /**
     * Request code for starting the CapsuleEditorActivity
     */
    private static final int REQUEST_CODE_CAPSULE_EDITOR = 1;

    /**
     * onCreate
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_capsule);

        // Get the Intent extras
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            this.mCapsule = extras.getParcelable("capsule");
            this.mAccount = extras.getParcelable("account");
            this.mOwned = this.mCapsule instanceof CapsuleOwnership;
        }

        // Close the Activity if required members are missing
        if (this.mCapsule == null || this.mAccount == null) {
            this.finish();
        }

        // Bundle the Fragment arguments
        Bundle bundle = new Bundle();
        bundle.putParcelable("capsule", this.mCapsule);
        bundle.putParcelable("account", this.mAccount);

        // Add any Fragments
        Fragment capsuleFragment = new CapsuleFragment();
        capsuleFragment.setArguments(bundle);
        Fragment discoveryFragment = null;
        if (!this.mOwned) {
            discoveryFragment = new DiscoveryFragment();
            discoveryFragment.setArguments(bundle);
        }
        if (savedInstanceState == null) {
            FragmentManager fragmentManager = this.getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.fragment_capsule, capsuleFragment);
            if (!this.mOwned) {
                fragmentTransaction.add(R.id.fragment_discovery, discoveryFragment);
            }
            fragmentTransaction.add(R.id.fragment_capsule_content, new CapsuleContentFragment());
            fragmentTransaction.commit();
        }

        // Setup the Toolbar
        this.mToolbar = Widgets.createToolbar(this, this.mCapsule.getName());
        this.mToolbar.inflateMenu(R.menu.capsule);
        this.mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close this Activity
                CapsuleActivity.this.setOkResult();
                CapsuleActivity.this.finish();
            }
        });
        this.mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_settings:
                        return false;
                    case R.id.action_edit:
                        Intent intent = new Intent(CapsuleActivity.this, CapsuleEditorActivity.class);
                        intent.putExtra("capsule", CapsuleActivity.this.mCapsule);
                        intent.putExtra("account", CapsuleActivity.this.mAccount);
                        CapsuleActivity.this.startActivityForResult(intent,
                                CapsuleActivity.REQUEST_CODE_CAPSULE_EDITOR);
                        return true;
                    default:
                        return false;
                }
            }
        });
        // Show the edit menu button if this Capsule is owned by the current Account
        if (this.mOwned) {
            Menu menu = this.mToolbar.getMenu();
            MenuItem editMenuItem = menu.findItem(R.id.action_edit);
            editMenuItem.setVisible(true);
        }
    }

    /**
     * onBackPressed
     */
    @Override
    public void onBackPressed() {
        this.setOkResult();
        super.onBackPressed();
    }

    /**
     * onActivityResult
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case (CapsuleActivity.REQUEST_CODE_CAPSULE_EDITOR):
                if (resultCode == Activity.RESULT_OK) {
                    // Flag it as modified
                    this.mModified = true;
                    // Get the new Capsule
                    this.mCapsule = data.getParcelableExtra("capsule");
                    // Get the Fragment containing the Capsule information
                    CapsuleFragment capsuleFragment = (CapsuleFragment) this.getSupportFragmentManager()
                            .findFragmentById(R.id.fragment_capsule);
                    // Populate the Fragment with the new Capsule
                    if (this.mCapsule != null && capsuleFragment != null) {
                        capsuleFragment.populateViews(this.mCapsule);
                        // Update the Toolbar
                        if (this.mToolbar != null) {
                            this.mToolbar.setSubtitle(this.mCapsule.getName());
                        }
                    }
                }
                break;

            default:
                break;
        }

    }

    /**
     * Callback for when the CapsuleFragment is not passed the required data
     *
     * Will close this Activity
     *
     * @param capsuleFragment The Fragment that is missing the data
     */
    @Override
    public void onMissingData(CapsuleFragment capsuleFragment) {
        this.getSupportFragmentManager().beginTransaction().remove(capsuleFragment).commit();
        this.setResult(Activity.RESULT_CANCELED);
        this.finish();
        Toast.makeText(this, this.getString(R.string.error_cannot_open_missing_data),
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Callback for when the DiscoveryFragment is not passed the required data
     *
     * Will close this Activity
     *
     * @param discoveryFragment The Fragment that is missing data
     */
    @Override
    public void onMissingData(DiscoveryFragment discoveryFragment) {
        this.getSupportFragmentManager().beginTransaction().remove(discoveryFragment).commit();
        this.setResult(Activity.RESULT_CANCELED);
        this.finish();
        Toast.makeText(this, this.getString(R.string.error_cannot_open_missing_data),
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Adds data to pass back when the Activity is closed under correct circumstances and
     * sets the result code as OK
     */
    private void setOkResult() {
        Intent intent = new Intent();
        intent.putExtra("capsule", this.mCapsule);
        intent.putExtra("modified", this.mModified);
        this.setResult(Activity.RESULT_OK, intent);
    }

}
