package com.brettnamba.capsules.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.LinearLayout;

/**
 * This Layout implements the Checkable interface so it can allow
 * ListView items to get the "checked" state for pre-Honeycomb Android.
 *
 * @author Brett Namba
 */
public class CheckableLinearLayout extends LinearLayout implements Checkable {

    /**
     * Indicates the checked state
     */
    private boolean mIsChecked;

    /**
     * Used in the overriding of onCreateDrawableState()
     */
    private static final int[] STATE_CHECKED = {android.R.attr.state_checked};

    /**
     * Constructor
     *
     * @param context
     */
    public CheckableLinearLayout(Context context) {
        super(context);
    }

    /**
     * Constructor
     *
     * @param context
     * @param attrs
     */
    public CheckableLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Change the checked state of the view
     *
     * @param checked The new checked state
     */
    @Override
    public void setChecked(boolean checked) {
        this.mIsChecked = checked;
        this.refreshDrawableState(); // Required for updating the drawable state of ListView items
    }

    /**
     * @return The current checked state of the view
     */
    @Override
    public boolean isChecked() {
        return this.mIsChecked;
    }

    /**
     * Change the checked state of the view to the inverse of its current state
     */
    @Override
    public void toggle() {
        this.setChecked(!this.mIsChecked);
    }

    /**
     * Required for updating the drawable state of ListView items in a CheckableLinearLayout
     *
     * @param extraSpace
     * @return
     */
    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (this.isChecked()) {
            CheckableLinearLayout.mergeDrawableStates(drawableState, STATE_CHECKED);
        }
        return drawableState;
    }

}
