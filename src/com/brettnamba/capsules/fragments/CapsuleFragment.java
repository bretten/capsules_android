package com.brettnamba.capsules.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
     * The layout View
     */
    private View mLayoutView;

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
            throw new ClassCastException(activity.toString() + " does not implement CapsuleFragmentListener");
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout View
        this.mLayoutView = inflater.inflate(R.layout.fragment_capsule, container, false);

        // Populate the Views
        if (this.mCapsule != null) {
            this.populateViews(this.mCapsule);
        }

        return this.mLayoutView;
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
     * Populates the Views with the Capsule data
     *
     * @param capsule The Capsule to use to populate the Views
     */
    public void populateViews(Capsule capsule) {
        if (this.mLayoutView != null) {
            // Capsule name
            TextView name = (TextView) this.mLayoutView.findViewById(R.id.fragment_capsule_info_name);
            name.setText(capsule.getName());
        }
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
