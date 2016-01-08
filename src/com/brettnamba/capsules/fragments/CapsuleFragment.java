package com.brettnamba.capsules.fragments;

import android.accounts.Account;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.brettnamba.capsules.R;
import com.brettnamba.capsules.dataaccess.Capsule;

/**
 * Fragment for displaying a Capsule
 *
 * @author Brett Namba
 */
public class CapsuleFragment extends Fragment {

    /**
     * The Capsule for this Activity
     */
    private Capsule mCapsule;

    /**
     * Listener that handles callbacks
     */
    private CapsuleFragmentListener mListener;

    /**
     * The TextView for the Capsule name
     */
    private TextView mCapsuleNameView;

    /**
     * The TextView for the Capsule's owner's username
     */
    private TextView mOwnerUsernameView;

    /**
     * The ImageView for the Memoir image
     */
    private ImageView mMemoirImageView;

    /**
     * The TextView for the Memoir title
     */
    private TextView mMemoirTitleView;

    /**
     * The TextView for the Memoir message
     */
    private TextView mMemoirMessageView;

    /**
     * The TextView for the rating of the Capsule
     */
    private TextView mTotalRatingView;

    /**
     * The TextView for the number of times the Capsule was discovered
     */
    private TextView mDiscoveryCountView;

    /**
     * The TextView for the number of times the Capsule was set as a favorite
     */
    private TextView mFavoriteCountView;

    /**
     * ProgressBar for the Memoir image
     */
    private ProgressBar mProgressBar;

    /**
     * onAttach
     *
     * @param activity The Activity that the Fragment is being attached to
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Make sure the Activity implements this Fragment's listener interface
        try {
            this.mListener = (CapsuleFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(
                    activity.toString() + " does not implement CapsuleFragmentListener");
        }
    }

    /**
     * onCreate
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get in the arguments passed from the Activity
        Bundle args = this.getArguments();
        if (args != null) {
            this.mCapsule = args.getParcelable("capsule");
        }
    }

    /**
     * onCreateView
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout View
        View view = inflater.inflate(R.layout.fragment_capsule, container, false);

        // Get references to the Views
        this.getViewReferences(view);

        // Populate the Views
        this.populateTextViews(this.mCapsule);

        return view;
    }

    /**
     * onActivityCreated
     *
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // If the Fragment is missing any required data, delegate handling to the listener
        if (this.mCapsule == null) {
            this.mListener.onMissingData(this);
        }
    }

    /**
     * Populates the TextViews with the Capsule data
     *
     * @param capsule The Capsule to use to populate the TextViews
     */
    public void populateTextViews(Capsule capsule) {
        if (capsule == null) {
            return;
        }

        // Capsule name
        if (this.mCapsuleNameView != null && capsule.getName() != null) {
            this.mCapsuleNameView.setText(capsule.getName());
        }
        // Capsule owner username
        if (this.mOwnerUsernameView != null && capsule.getUser() != null &&
                capsule.getUser().getUsername() != null) {
            this.mOwnerUsernameView.setText(capsule.getUser().getUsername());
        }
        // Memoir title
        if (this.mMemoirTitleView != null && capsule.getMemoir() != null &&
                capsule.getMemoir().getTitle() != null) {
            this.mMemoirTitleView.setText(capsule.getMemoir().getTitle());
        }
        // Memoir message
        if (this.mMemoirMessageView != null && capsule.getMemoir() != null &&
                capsule.getMemoir().getMessage() != null) {
            this.mMemoirMessageView.setText(capsule.getMemoir().getMessage());
        }
        // Total rating
        if (this.mTotalRatingView != null) {
            this.mTotalRatingView.setText(String.valueOf(capsule.getTotalRating()));
        }
        // Discovery count
        if (this.mDiscoveryCountView != null) {
            this.mDiscoveryCountView.setText(String.valueOf(capsule.getDiscoveryCount()));
        }
        // Favorite count
        if (this.mFavoriteCountView != null) {
            this.mFavoriteCountView.setText(String.valueOf(capsule.getFavoriteCount()));
        }
    }

    /**
     * Sets the Memoir image Bitmap
     *
     * @param bitmap The Memoir image bitmap
     */
    public void setMemoirImageView(Bitmap bitmap) {
        if (this.mMemoirImageView == null || bitmap == null) {
            return;
        }

        // Hide the ProgressBar
        this.mProgressBar.setVisibility(View.GONE);
        // Set the Bitmap on the ImageView
        this.mMemoirImageView.setImageBitmap(bitmap);
    }

    /**
     * Gets references to the layout View's nested Views
     *
     * @param layout The layout View
     */
    private void getViewReferences(View layout) {
        this.mCapsuleNameView = (TextView) layout.findViewById(R.id.fragment_capsule_name);
        this.mOwnerUsernameView =
                (TextView) layout.findViewById(R.id.fragment_capsule_owner_username);
        this.mMemoirImageView = (ImageView) layout.findViewById(R.id.fragment_capsule_memoir_image);
        this.mMemoirTitleView = (TextView) layout.findViewById(R.id.fragment_capsule_memoir_title);
        this.mMemoirMessageView =
                (TextView) layout.findViewById(R.id.fragment_capsule_memoir_message);
        this.mTotalRatingView = (TextView) layout.findViewById(R.id.fragment_capsule_total_rating);
        this.mDiscoveryCountView =
                (TextView) layout.findViewById(R.id.fragment_capsule_discovery_count);
        this.mFavoriteCountView =
                (TextView) layout.findViewById(R.id.fragment_capsule_favorite_count);
        this.mProgressBar = (ProgressBar) layout.findViewById(R.id.fragment_capsule_progress_bar);
    }

    /**
     * Instantiates a CapsuleFragment given a Capsule and Account
     *
     * @param capsule The Capsule that will populate the Fragment
     * @param account The current Account
     * @return The CapsuleFragment instance
     */
    public static CapsuleFragment createInstance(Capsule capsule, Account account) {
        CapsuleFragment fragment = new CapsuleFragment();

        // Bundle the Fragment arguments
        Bundle bundle = new Bundle();
        bundle.putParcelable("capsule", capsule);
        bundle.putParcelable("account", account);
        // Set the arguments on the Fragment
        fragment.setArguments(bundle);

        return fragment;
    }

    /**
     * Listener that provides event callbacks for this Fragment
     */
    public interface CapsuleFragmentListener {

        /**
         * Should handle the case where the Fragment is not passed the required data
         *
         * @param capsuleFragment The Fragment that is missing the data
         */
        void onMissingData(CapsuleFragment capsuleFragment);

    }

}
