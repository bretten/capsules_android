package com.brettnamba.capsules.widget;

import android.accounts.Account;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.brettnamba.capsules.R;

/**
 * Extension of ArrayAdapter to handle Android Accounts
 */
public class AccountArrayAdapter extends ArrayAdapter<Account> {

    /**
     * The current context
     */
    private Context mContext;

    /**
     * Constructor
     *
     * @param context The current context
     * @param resource The resource layout for an individual item
     * @param objects Collection of Accounts to use as items
     */
    public AccountArrayAdapter(Context context, int resource, Account[] objects) {
        super(context, resource, objects);
        this.mContext = context;
    }

    /**
     * Gets and inflates the View at the specified position
     *
     * Uses the ViewHolder pattern to  see if the View has already been used so it can
     * potentially re-use it
     *
     * @param position
     * @param convertView
     * @param parent
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // The current Account
        Account account = this.getItem(position);

        // Use the ViewHolder pattern to keep a reference to already inflated Views
        ViewHolder viewHolder;
        // If the View is null, then it is being shown for the first time
        if (convertView == null) {
            // Create a ViewHolder to store a reference to the View
            viewHolder = new ViewHolder();
            // Inflate the layout for this item
            LayoutInflater inflater = LayoutInflater.from(this.mContext);
            convertView = inflater.inflate(R.layout.account_list_item, parent, false);
            // Store references to the View in the ViewHolder
            viewHolder.textView = (TextView) convertView.findViewById(R.id.account_list_item_text);
            viewHolder.position = position;
            convertView.setTag(viewHolder);
        } else {
            // The View has already been used so re-use it
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Set the data on the item's Views
        viewHolder.textView.setText(account.name);

        return convertView;
    }

    /**
     * ViewHolder for the Account item
     */
    private static class ViewHolder {
        TextView textView;
        int position;
    }

}
