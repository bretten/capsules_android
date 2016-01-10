package com.brettnamba.capsules.fragments;

import android.accounts.Account;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.brettnamba.capsules.R;
import com.brettnamba.capsules.dataaccess.Capsule;
import com.brettnamba.capsules.dataaccess.Discovery;
import com.brettnamba.capsules.widget.RatingControl;

/**
 * Fragment for a Discovery that allows for it to be viewed and updated
 */
public class DiscoveryFragment extends Fragment implements
        RatingControl.RatingListener {

    /**
     * The Capsule
     */
    private Capsule mCapsule;

    /**
     * Listener that handles the callbacks
     */
    private DiscoveryFragmentListener mListener;

    /**
     * The Account that is viewing this Fragment
     */
    private Account mAccount;

    /**
     * The control for rating a Discovery
     */
    private RatingControl mRatingControl;

    /**
     * The favorite toggle for a Discovery
     */
    private ToggleButton mFavoriteToggle;

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
            this.mListener = (DiscoveryFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(
                    activity.toString() + " does not implement DiscoveryFragmentListener");
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
        // Get the arguments passed in from the Activity
        Bundle args = this.getArguments();
        if (args != null) {
            this.mCapsule = args.getParcelable("capsule");
            this.mAccount = args.getParcelable("account");
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
        View view = inflater.inflate(R.layout.fragment_discovery, container, false);

        // Populate the Views
        if (this.mCapsule != null && this.mCapsule.isDiscovery() && this.mAccount != null) {
            // Set up the Rating control
            this.mRatingControl = (RatingControl) view.findViewById(R.id.fragment_discovery_rating);
            this.mRatingControl.setListener(this);

            // Set up the favorite toggle
            this.mFavoriteToggle =
                    (ToggleButton) view.findViewById(R.id.fragment_discovery_favorite);
            this.mFavoriteToggle.setOnCheckedChangeListener(
                    new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (DiscoveryFragment.this.mListener != null) {
                                DiscoveryFragment.this.mListener.onFavorite(isChecked);
                            }
                        }
                    });

            // Match the state of the inputs to the Discovery
            this.matchStateToDiscovery(this.mCapsule.getDiscovery());
        }

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
        if (this.mCapsule == null || this.mAccount == null) {
            this.mListener.onMissingData(this);
        }
    }

    /**
     * Updates the Discovery on the background thread when the down button is pressed
     */
    @Override
    public void onRateDown() {
        if (this.mListener != null) {
            this.mListener.onRateDown();
        }
    }

    /**
     * Updates the Discovery on the background thread when the up button is pressed
     */
    @Override
    public void onRateUp() {
        if (this.mListener != null) {
            this.mListener.onRateUp();
        }
    }

    /**
     * Updates the Discovery on the background thread when the rating is removed
     */
    @Override
    public void onRemoveRating() {
        if (this.mListener != null) {
            this.mListener.onRemoveRating();
        }
    }

    /**
     * Matches the state of the inputs to the Discovery
     *
     * @param discovery The Discovery to match the inputs to
     */
    public void matchStateToDiscovery(Discovery discovery) {
        // Match the favorite state
        this.mFavoriteToggle.setChecked(discovery.isFavorite());
        // Match the rating
        this.mRatingControl.setUpButtonChecked(false);
        this.mRatingControl.setDownButtonChecked(false);
        if (discovery.isRatedUp()) {
            this.mRatingControl.setUpButtonChecked(true);
        } else if (discovery.isRatedDown()) {
            this.mRatingControl.setDownButtonChecked(true);
        }
    }

    /**
     * Disables the buttons in this Fragment
     */
    public void disableButtons() {
        if (this.mRatingControl != null) {
            this.mRatingControl.setButtonsEnabled(false);
        }
        if (this.mFavoriteToggle != null) {
            this.mFavoriteToggle.setEnabled(false);
        }
    }

    /**
     * Enables the buttons in this Fragment
     */
    public void enableButtons() {
        if (this.mRatingControl != null) {
            this.mRatingControl.setButtonsEnabled(true);
        }
        if (this.mFavoriteToggle != null) {
            this.mFavoriteToggle.setEnabled(true);
        }
    }

    /**
     * Instantiates a DiscoveryFragment given a Capsule and Account
     *
     * @param capsule The Capsule that will populate the Fragment
     * @param account The current Account
     * @return The DiscoveryFragment instance
     */
    public static DiscoveryFragment createInstance(Capsule capsule, Account account) {
        DiscoveryFragment fragment = new DiscoveryFragment();

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
    public interface DiscoveryFragmentListener {

        /**
         * Should handle the case where the Fragment is not passed the required data
         *
         * @param discoveryFragment The Fragment that is missing data
         */
        void onMissingData(DiscoveryFragment discoveryFragment);

        /**
         * Executes when the rate up button is clicked
         */
        void onRateUp();

        /**
         * Executes when the rate down button is clicked
         */
        void onRateDown();

        /**
         * Executes when the rating is removed from the RatingControl
         */
        void onRemoveRating();

        /**
         * Executes when the favorite ToggleButton is clicked
         *
         * @param isFavorite The state of the button
         */
        void onFavorite(boolean isFavorite);

    }

}
