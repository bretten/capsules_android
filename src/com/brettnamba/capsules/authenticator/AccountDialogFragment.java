package com.brettnamba.capsules.authenticator;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.brettnamba.capsules.Constants;
import com.brettnamba.capsules.R;
import com.brettnamba.capsules.widget.AccountArrayAdapter;

/**
 * Allows the user to switch Accounts
 *
 * Provides an interface with callbacks that hosting Activities can implement
 */
public class AccountDialogFragment extends DialogFragment {

    /**
     * Collection of the Accounts available
     */
    private Account[] mAccounts;

    /**
     * The hosting Activity that will implement the callback interface
     */
    private AccountDialogListener mListener;

    /**
     * onCreateDialog
     *
     * Sets up the Dialog and the ListView containing the Accounts
     *
     * @param savedInstanceState
     * @return
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        // Layout inflater
        LayoutInflater inflater = this.getActivity().getLayoutInflater();
        // Get the layout View
        LinearLayout view = (LinearLayout) inflater.inflate(R.layout.fragment_account, null);

        // Get the ListView
        ListView listView = (ListView) view.findViewById(R.id.fragment_account_list);
        // Populate the ListView with Accounts
        AccountManager accountManager = AccountManager.get(this.getActivity());
        this.mAccounts = accountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
        listView.setAdapter(new AccountArrayAdapter(
                this.getActivity().getApplicationContext(),
                R.layout.account_list_item,
                this.mAccounts
        ));
        // Set the click listener for the ListView
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Delegate to the Fragment's method
                AccountDialogFragment.this.clickItem(position);
            }
        });

        // Set the listener for the "add account" button
        Button button = (Button) view.findViewById(R.id.fragment_account_add);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountDialogFragment.this.getActivity(), LoginActivity.class);
                AccountDialogFragment.this.getActivity().startActivity(intent);
            }
        });

        // Set the View for the Dialog
        builder.setView(view)
                .setTitle(this.getString(R.string.title_accounts));

        return builder.create();
    }

    /**
     * onAttach
     *
     * Sets the host Activity as the listener that implements the callback interface
     *
     * @param activity The new host Activity
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Make sure the Activity implements this Fragment's listener interface
        try {
            this.mListener = (AccountDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " does not implement AccountDialogListener");
        }
    }

    /**
     * Handles clicking on an item by delegating to the listener
     *
     * @param position The position of the item clicked
     */
    private void clickItem(int position) {
        if (this.mAccounts != null && this.mListener != null) {
            this.mListener.onAccountItemClick(this, this.mAccounts[position]);
        }
    }

    /**
     * Callback interface that is implemented by the host Activity
     */
    public interface AccountDialogListener {
        /**
         * Should handle clicks on a specific Account from the collection
         *
         * @param dialog A reference to the DialogFragment
         * @param account The Account that was selected
         */
        void onAccountItemClick(AccountDialogFragment dialog, Account account);
    }

}
