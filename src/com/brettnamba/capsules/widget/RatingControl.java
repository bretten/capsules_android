package com.brettnamba.capsules.widget;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import com.brettnamba.capsules.R;

/**
 * Compound control that provides "rate up" and "rate down" functionality to the UI
 */
public class RatingControl extends LinearLayout {

    /**
     * The "rate up" button
     */
    private ToggleButton mUpButton;

    /**
     * The "rate down" button
     */
    private ToggleButton mDownButton;

    /**
     * Listener that will handle the callbacks
     */
    private RatingListener mListener;

    /**
     * Constructor
     *
     * @param context Context the View is running in
     */
    public RatingControl(Context context) {
        this(context, null);
    }

    /**
     * Constructor
     *
     * @param context Context the View is running in
     * @param attrs   The XML attributes
     */
    public RatingControl(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Setup
        this.setup(context);
    }

    /**
     * Sets the listener that will handle the callbacks and also sets the listeners on the
     * up and down buttons that delegate the work
     *
     * @param listener Listener that will handle the callbacks
     */
    public void setListener(RatingListener listener) {
        this.mListener = listener;
        // Set up the button listeners
        this.mUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear the down button
                RatingControl.this.mDownButton.setChecked(false);
                // Delegate the check state handling to the listener
                if (((ToggleButton) v).isChecked()) {
                    RatingControl.this.mListener.onRateUp();
                } else {
                    RatingControl.this.mListener.onRemoveRating();
                }
            }
        });
        this.mDownButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear the up button
                RatingControl.this.mUpButton.setChecked(false);
                // Delegate the check state handling to the listener
                if (((ToggleButton) v).isChecked()) {
                    RatingControl.this.mListener.onRateDown();
                } else {
                    RatingControl.this.mListener.onRemoveRating();
                }
            }
        });
        this.mUpButton.setOnCheckedChangeListener(ON_CHECKED_LISTENER);
        this.mDownButton.setOnCheckedChangeListener(ON_CHECKED_LISTENER);
    }

    /**
     * Sets the checked state of the up button
     *
     * @param checked True is checked, false is not
     */
    public void setUpButtonChecked(boolean checked) {
        this.mUpButton.setChecked(checked);
    }

    /**
     * Sets the checked state of the down button
     *
     * @param checked True is checked, false is not
     */
    public void setDownButtonChecked(boolean checked) {
        this.mDownButton.setChecked(checked);
    }

    /**
     * Sets the enabled state of the buttons
     *
     * @param enabled True is enabled, false is disabled
     */
    public void setButtonsEnabled(boolean enabled) {
        this.mUpButton.setEnabled(enabled);
        this.mDownButton.setEnabled(enabled);
    }

    /**
     * Handles setup of the Views
     *
     * @param context The Context the View runs in
     */
    private void setup(Context context) {
        // Instantiate the toggles
        this.mUpButton = new ToggleButton(context);
        this.mDownButton = new ToggleButton(context);
        this.addView(this.mUpButton);
        this.addView(this.mDownButton);
        // Remove the text
        this.mUpButton.setText(null);
        this.mUpButton.setTextOff(null);
        this.mUpButton.setTextOn(null);
        this.mDownButton.setText(null);
        this.mDownButton.setTextOff(null);
        this.mDownButton.setTextOn(null);
        // Set the drawables
        this.mUpButton.setBackgroundResource(R.drawable.ic_thumb_up_grey);
        this.mDownButton.setBackgroundResource(R.drawable.ic_thumb_down_grey);
    }

    /**
     * Check listener for the up and down buttons that changes the visual state
     */
    public static final CompoundButton.OnCheckedChangeListener ON_CHECKED_LISTENER
            = new CompoundButton.OnCheckedChangeListener() {

        /**
         * Listens for changes in the checked state of the button
         *
         * @param buttonView The button that was checked
         * @param isChecked True means it was checked, false means it was un-checked
         */
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                // Change the color of the button
                buttonView.getBackground().setColorFilter(R.color.primary_color, PorterDuff.Mode.SRC_ATOP);
            } else {
                // Remove the color
                buttonView.getBackground().clearColorFilter();
            }
        }

    };

    /**
     * Listener interface for handling callbacks on the RatingControl
     */
    public interface RatingListener {

        /**
         * Handles when the up rating button is pressed
         */
        void onRateUp();

        /**
         * Handles when the down rating button is pressed
         */
        void onRateDown();

        /**
         * Handles when the rating is removed
         */
        void onRemoveRating();

    }

}
