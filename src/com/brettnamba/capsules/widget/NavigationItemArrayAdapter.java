package com.brettnamba.capsules.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.brettnamba.capsules.R;

import java.util.ArrayList;

/**
 * Implementation of ArrayAdapter tailored toward a navigation drawer with normal and header
 * items
 *
 * @author Brett Namba
 */
public class NavigationItemArrayAdapter extends ArrayAdapter<NavigationDrawerItem> {

    /**
     * The context of the application
     */
    private Context mContext;

    /**
     * Constructor
     *
     * @param context
     * @param resource
     * @param objects
     */
    public NavigationItemArrayAdapter(Context context, int resource, ArrayList<NavigationDrawerItem> objects) {
        super(context, resource, objects);
        this.mContext = context;
    }

    /**
     * Gets and inflates the View at the specified position
     *
     * Depending on if the item is a normal item or a header, it will set the contained
     * Views accordingly
     *
     * This method also checks to see if a View of the same type was already shown before
     * so it can reuse the View
     *
     * @param position
     * @param convertView
     * @param parent
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the drawer item
        NavigationDrawerItem item = this.getItem(position);

        // Check if the item is a header
        if (item.isHeader()) {
            // Cast to the header type
            HeaderDrawerItem headerItem = (HeaderDrawerItem) item;

            // See if the view is being reused
            HeaderViewHolder viewHolder;
            if (convertView == null) {
                // The view has not been shown yet, so find the necessary views and store them in a ViewHolder
                viewHolder = new HeaderViewHolder();
                // Inflate the layout
                LayoutInflater inflater = LayoutInflater.from(this.mContext);
                convertView = inflater.inflate(R.layout.navigation_drawer_header_item, parent, false);
                // Store the views
                viewHolder.textView = (TextView) convertView.findViewById(R.id.navigation_drawer_item_text);
                viewHolder.position = position;
                convertView.setTag(viewHolder);
            } else {
                // The view has already been shown, so reuse it
                viewHolder = (HeaderViewHolder) convertView.getTag();
            }

            // Set the text
            viewHolder.textView.setText(headerItem.getText());

            return convertView;
        } else {
            // Cast to the normal type
            NormalDrawerItem normalItem = (NormalDrawerItem) item;

            // See if the view is being reused
            NormalViewHolder viewHolder;
            if (convertView == null) {
                // The view has not been shown yet, so find the necessary views and store them in a ViewHolder
                viewHolder = new NormalViewHolder();
                // Inflate the layout
                LayoutInflater inflater = LayoutInflater.from(this.mContext);
                convertView = inflater.inflate(R.layout.navigation_drawer_normal_item, parent, false);
                // Store the views
                viewHolder.imageView = (ImageView) convertView.findViewById(R.id.navigation_drawer_item_icon);
                viewHolder.textView = (TextView) convertView.findViewById(R.id.navigation_drawer_item_text);
                viewHolder.position = position;
                convertView.setTag(viewHolder);
            } else {
                // The view has already been shown, so reuse it
                viewHolder = (NormalViewHolder) convertView.getTag();
            }

            // Set the icon and the text
            viewHolder.imageView.setImageDrawable(normalItem.getDrawable());
            viewHolder.textView.setText(normalItem.getText());

            return convertView;
        }
    }

    /**
     * Gets the item view type at the specified position
     *
     * @param position
     * @return An integer representing the type
     */
    @Override
    public int getItemViewType(int position) {
        NavigationDrawerItem item = this.getItem(position);
        if (item.isHeader()) {
            return 0;
        } else {
            return 1;
        }
    }

    /**
     * Gets the number of view types
     *
     * @return The number of view types
     */
    @Override
    public int getViewTypeCount() {
        return 2;
    }

    /**
     * By default, all items are not enabled
     *
     * @return false
     */
    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    /**
     * Determines if the item at the specified position is enabled
     *
     * Headers are not enabled
     *
     * @param position
     * @return Whether or not the item is enabled
     */
    @Override
    public boolean isEnabled(int position) {
        NavigationDrawerItem item = this.getItem(position);
        return !item.isHeader();
    }

    /**
     * ViewHolder for storing Views for a HeaderDrawerItem
     */
    private static class HeaderViewHolder {
        TextView textView;
        int position;
    }

    /**
     * ViewHolder for storing Views for a NormalDrawerItem
     */
    private static class NormalViewHolder {
        ImageView imageView;
        TextView textView;
        int position;
    }

}
