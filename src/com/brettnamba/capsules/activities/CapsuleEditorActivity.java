package com.brettnamba.capsules.activities;

import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.brettnamba.capsules.R;
import com.brettnamba.capsules.dataaccess.Capsule;
import com.brettnamba.capsules.fragments.CapsuleEditorFragment;
import com.brettnamba.capsules.util.Widgets;

import java.util.List;

/**
 * Activity that allows a Capsule to be created or edited
 *
 * @author Brett Namba
 */
public class CapsuleEditorActivity extends FragmentActivity implements
        CapsuleEditorFragment.CapsuleEditorFragmentListener {

    /**
     * The Account that is editing
     */
    private Account mAccount;

    /**
     * Fragment used to edit the Capsule
     */
    private CapsuleEditorFragment mEditorFragment;

    /**
     * Displays messages to the user
     */
    private TextView mMessageView;

    /**
     * Key for the CapsuleEditorFragment to be used in transactions
     */
    private static final String KEY_CAPSULE_EDITOR_FRAGMENT = "capsule_editor_fragment";

    /**
     * onCreate
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_capsule_editor);

        // Get the Intent extras
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            this.mAccount = extras.getParcelable("account");
        }

        // Close the Activity if required members are missing
        if (this.mAccount == null) {
            this.finish();
        }

        // FragmentManager
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        // See if the Activity is being recreated
        if (savedInstanceState == null) {
            // Instantiate the Fragment for editing Capsules
            this.mEditorFragment = CapsuleEditorFragment.createInstance(this.mAccount);
            // FragmentTransaction
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.fragment_capsule_editor, this.mEditorFragment,
                    CapsuleEditorActivity.KEY_CAPSULE_EDITOR_FRAGMENT);
            fragmentTransaction.commit();
        } else {
            // The Activity has previous state data
            this.mEditorFragment = (CapsuleEditorFragment) fragmentManager.getFragment(
                    savedInstanceState, CapsuleEditorActivity.KEY_CAPSULE_EDITOR_FRAGMENT);
        }

        // Setup the Toolbar
        Toolbar toolbar = Widgets.createToolbar(this, this.getString(R.string.map_new_capsule));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close this Activity
                CapsuleEditorActivity.this.finish();
            }
        });

        // Find the messages View
        this.mMessageView = (TextView) this.findViewById(R.id.messages);
    }

    /**
     * onSaveInstanceState
     *
     * @param outState Bundle containing saved Fragment states
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the state of the CapsuleEditorFragment
        if (this.mEditorFragment != null) {
            this.getSupportFragmentManager().putFragment(outState,
                    CapsuleEditorActivity.KEY_CAPSULE_EDITOR_FRAGMENT, this.mEditorFragment);
        }
    }

    /**
     * Callback for when the CapsuleEditorFragment is not passed the required data
     *
     * @param capsuleEditorFragment The Fragment that is missing the data
     */
    @Override
    public void onMissingData(CapsuleEditorFragment capsuleEditorFragment) {
        this.getSupportFragmentManager().beginTransaction().remove(capsuleEditorFragment).commit();
        this.setResult(Activity.RESULT_CANCELED);
        this.finish();
        Toast.makeText(this, this.getString(R.string.error_cannot_open_missing_data),
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Callback for when the CapsuleEditorFragment tried to save invalid Capsule data
     *
     * @param capsuleEditorFragment The Fragment that attempted the save
     * @param capsule               The Capsule with invalid data
     * @param messages              The error messages
     */
    @Override
    public void onInvalidCapsuleData(CapsuleEditorFragment capsuleEditorFragment, Capsule capsule,
                                     List<String> messages) {
        // Show the validation error messages
        this.setMessages(messages);
    }

    /**
     * Callback for when the Fragment triggers the file upload chooser
     *
     * @param capsuleEditorFragment The Fragment that is choosing the upload source
     */
    @Override
    public void onChooseUploadSource(CapsuleEditorFragment capsuleEditorFragment) {
        // Clear any existing validation messages
        this.clearMessages();
    }

    /**
     * Callback for when the CapsuleEditorFragment has successfully saved the Capsule data
     *
     * @param capsuleEditorFragment The Fragment that saved the data
     * @param capsule               The Capsule that was used to save
     */
    @Override
    public void onSaveSuccess(CapsuleEditorFragment capsuleEditorFragment, Capsule capsule) {
        // Clear any validation messages
        this.clearMessages();
        // Place the saved Capsule in the Intent
        Intent intent = new Intent();
        intent.putExtra("capsule", capsule);
        // Set the result as OK and finish
        this.setResult(Activity.RESULT_OK, intent);
        this.finish();
    }

    /**
     * Sets the collection of messages and displays them to the user
     *
     * @param messages Collection of messages to be displayed
     */
    private void setMessages(List<String> messages) {
        if (this.mMessageView != null) {
            this.mMessageView.setText(TextUtils.join("\n", messages));
            this.mMessageView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Clears any messages and hides the View
     */
    private void clearMessages() {
        if (this.mMessageView != null) {
            this.mMessageView.setVisibility(View.GONE);
            this.mMessageView.setText("");
        }
    }

}
