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
import com.brettnamba.capsules.dataaccess.CapsuleOwnership;
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
     * The Capsule
     */
    private CapsuleOwnership mCapsule;

    /**
     * The Account that is editing
     */
    private Account mAccount;

    /**
     * Displays messages to the user
     */
    private TextView mMessageView;

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
            this.mCapsule = extras.getParcelable("capsule");
            this.mAccount = extras.getParcelable("account");
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
        CapsuleEditorFragment capsuleEditorFragment = new CapsuleEditorFragment();
        capsuleEditorFragment.setArguments(bundle);
        if (savedInstanceState == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.fragment_capsule_editor, capsuleEditorFragment);
            fragmentTransaction.commit();
        }

        // Setup the Toolbar
        Toolbar toolbar = Widgets.createToolbar(this, this.mCapsule.getName());
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
    public void onInvalidCapsuleData(CapsuleEditorFragment capsuleEditorFragment, CapsuleOwnership capsule,
                                     List<String> messages) {
        // Show the validation error messages
        this.setMessages(messages);
    }

    /**
     * Callback for when the CapsuleEditorFragment has successfully saved the Capsule data
     *
     * @param capsuleEditorFragment The Fragment that saved the data
     * @param capsule               The Capsule that was used to save
     */
    @Override
    public void onSaveSuccess(CapsuleEditorFragment capsuleEditorFragment, CapsuleOwnership capsule) {
        // Clear any validation messages
        this.clearMessages();
        // Set the Capsule
        this.mCapsule = capsule;
        // Place the saved Capsule in the Intent
        Intent intent = new Intent();
        intent.putExtra("capsule", this.mCapsule);
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
