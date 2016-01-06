package com.brettnamba.capsules.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.brettnamba.capsules.R;
import com.brettnamba.capsules.dataaccess.Capsule;
import com.brettnamba.capsules.dataaccess.User;

/**
 * ArrayAdapter for displaying Capsule objects in a ListView.
 *
 * @author Brett Namba
 */
public class CapsuleArrayAdapter extends ArrayAdapter<Capsule> {

    /**
     * The current Context
     */
    private Context mContext;

    /**
     * Constructor
     *
     * @param context  The current Context
     * @param resource The layout resource file to use when inflating the View for each ListView
     *                 item
     */
    public CapsuleArrayAdapter(Context context, int resource) {
        super(context, resource);
        this.mContext = context;
    }

    /**
     * Inflates the View for the Capsule list item in the ListView at the specified position.  Uses
     * the ViewHolder pattern to re-use View references.
     *
     * @param position    The position in the Adapter of the line item View being rendered
     * @param convertView The reference to the line item View, which may or may not be null
     *                    depending on if the View type was already rendered
     * @param parent      The parent ViewGroup for the line item View
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the Capsule at the specified position
        Capsule capsule = this.getItem(position);

        // Use the ViewHolder pattern to keep a reference to already inflated Views
        ViewHolder viewHolder;
        // If the View is null, then it is being shown for the first time
        if (convertView == null) {
            // Create a ViewHolder to store a reference to the Views
            viewHolder = new ViewHolder();
            // Inflate the layout for this item
            LayoutInflater inflater = LayoutInflater.from(this.mContext);
            convertView = inflater.inflate(R.layout.capsule_list_item, parent, false);
            // Store references to the Views in the ViewHolder
            viewHolder.titleView =
                    (TextView) convertView.findViewById(R.id.capsule_list_item_title);
            viewHolder.ownerView =
                    (TextView) convertView.findViewById(R.id.capsule_list_item_owner_username);
            viewHolder.totalRatingView =
                    (TextView) convertView.findViewById(R.id.capsule_list_item_total_rating);
            viewHolder.discoveryCountView =
                    (TextView) convertView.findViewById(R.id.capsule_list_item_discovery_count);
            viewHolder.favoriteCountView =
                    (TextView) convertView.findViewById(R.id.capsule_list_item_favorite_count);

            convertView.setTag(viewHolder);
        } else {
            // Get the ViewHolder that already has references to the Views
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Populate the Views
        viewHolder.titleView.setText(capsule.getName());
        User user = capsule.getUser();
        if (user != null && user.getUsername() != null) {
            viewHolder.ownerView.setVisibility(View.VISIBLE);
            viewHolder.ownerView.setText(user.getUsername());
        } else {
            viewHolder.ownerView.setVisibility(View.GONE);
        }
        viewHolder.totalRatingView.setText(String.valueOf(capsule.getTotalRating()));
        viewHolder.discoveryCountView.setText(String.valueOf(capsule.getDiscoveryCount()));
        viewHolder.favoriteCountView.setText(String.valueOf(capsule.getFavoriteCount()));

        return convertView;
    }

    /**
     * ViewHolder for a Capsule ListView item
     */
    private static class ViewHolder {
        TextView titleView;
        TextView ownerView;
        TextView totalRatingView;
        TextView discoveryCountView;
        TextView favoriteCountView;
    }

}
