package com.brettnamba.capsules.fragments;

import android.accounts.Account;
import android.app.Activity;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.brettnamba.capsules.R;
import com.brettnamba.capsules.dataaccess.CapsuleDiscovery;
import com.brettnamba.capsules.os.AsyncListenerTask;
import com.brettnamba.capsules.os.UpdateDiscoveryTask;
import com.brettnamba.capsules.provider.CapsuleContract;
import com.brettnamba.capsules.provider.CapsuleOperations;
import com.brettnamba.capsules.widget.RatingControl;

/**
 * Fragment for a Discovery that allows for it to be viewed and updated
 */
public class DiscoveryFragment extends Fragment implements
        RatingControl.RatingListener,
        AsyncListenerTask.UpdateDiscoveryTaskListener {

    /**
     * The Capsule
     */
    private CapsuleDiscovery mCapsule;

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
            throw new ClassCastException(activity.toString() + " does not implement DiscoveryFragmentListener");
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout View
        View view = inflater.inflate(R.layout.fragment_discovery, container, false);

        // Populate the Views
        if (this.mCapsule != null && this.mAccount != null) {
            // Rating control
            this.mRatingControl = (RatingControl) view.findViewById(R.id.fragment_discovery_rating);
            this.mRatingControl.setListener(this);
            if (this.mCapsule.getRating() >= 1) {
                this.mRatingControl.setUpButtonChecked(true);
            } else if (this.mCapsule.getRating() <= -1) {
                this.mRatingControl.setDownButtonChecked(true);
            }
            // Favorite toggle
            this.mFavoriteToggle = (ToggleButton) view.findViewById(R.id.fragment_discovery_favorite);
            if (this.mCapsule.getFavorite() > 0) {
                this.mFavoriteToggle.setChecked(true);
                this.mFavoriteToggle.getBackground().setColorFilter(R.color.primary_color, PorterDuff.Mode.SRC_ATOP);
            }
            this.mFavoriteToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        DiscoveryFragment.this.mCapsule.setFavorite(1);
                        buttonView.getBackground().setColorFilter(R.color.primary_color, PorterDuff.Mode.SRC_ATOP);
                    } else {
                        DiscoveryFragment.this.mCapsule.setFavorite(0);
                        buttonView.getBackground().clearColorFilter();
                    }
                    new UpdateDiscoveryTask(DiscoveryFragment.this).execute(DiscoveryFragment.this.mCapsule);
                }
            });
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
        if (this.mCapsule != null) {
            this.mCapsule.setRating(-1);
            new UpdateDiscoveryTask(this).execute(this.mCapsule);
        }
    }

    /**
     * Updates the Discovery on the background thread when the up button is pressed
     */
    @Override
    public void onRateUp() {
        if (this.mCapsule != null) {
            this.mCapsule.setRating(1);
            new UpdateDiscoveryTask(this).execute(this.mCapsule);
        }
    }

    /**
     * Updates the Discovery on the background thread when the rating is removed
     */
    @Override
    public void onRemoveRating() {
        if (this.mCapsule != null) {
            this.mCapsule.setRating(0);
            new UpdateDiscoveryTask(this).execute(this.mCapsule);
        }
    }

    /**
     * Saves the updated Discovery on the background thread
     *
     * @param params The Discovery to update
     * @return The result of the update
     */
    @Override
    public boolean duringUpdateDiscovery(CapsuleDiscovery... params) {
        return CapsuleOperations.Discoveries.save(this.getActivity().getContentResolver(),
                params[0], CapsuleContract.SyncStateAction.DIRTY);
    }

    /**
     * Disables the buttons before the Discovery is updated on the background thread
     */
    @Override
    public void onPreUpdateDiscovery() {
        this.mRatingControl.setButtonsEnabled(false);
        this.mFavoriteToggle.setEnabled(false);
    }

    /**
     * Re-enables the buttons after the background work has finished
     *
     * @param result The result of the update
     */
    @Override
    public void onPostUpdateDiscovery(Boolean result) {
        this.mRatingControl.setButtonsEnabled(true);
        this.mFavoriteToggle.setEnabled(true);
    }

    /**
     * Re-enables the buttons if the background work was cancelled
     */
    @Override
    public void onUpdateDiscoveryCancelled() {
        this.mRatingControl.setButtonsEnabled(true);
        this.mFavoriteToggle.setEnabled(true);
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

    }

}
