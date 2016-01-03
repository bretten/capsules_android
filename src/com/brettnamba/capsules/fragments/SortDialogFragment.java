package com.brettnamba.capsules.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.brettnamba.capsules.R;
import com.brettnamba.capsules.http.CapsuleRequestParameters;
import com.brettnamba.capsules.http.RequestContract;

import java.util.Arrays;
import java.util.List;

/**
 * Fragment that displays sort options for Capsules.
 */
public class SortDialogFragment extends DialogFragment {

    /**
     * The listener that handles the Dialog's callbacks
     */
    private SortDialogListener mListener;

    /**
     * The request parameters representing the initial state of the sort inputs
     */
    private CapsuleRequestParameters mRequestParams;

    /**
     * Input for holding search text
     */
    private EditText mSearchEditText;

    /**
     * Input for selecting the sort options
     */
    private Spinner mSortSpinner;

    /**
     * Adapter for the SortSpinner input
     */
    private ArrayAdapter<String> mSortSpinnerAdapter;

    /**
     * Collection of sort request parameter values
     */
    private List<String> mSortValues;

    /**
     * Instantiates a SortDialogFragment with the specified request parameters
     *
     * @param params The request parameters to instantiate the SortDialogFragment with
     * @return The newly instantiated SortDialogFragment
     */
    public static SortDialogFragment createInstance(CapsuleRequestParameters params) {
        // Instantiate the Fragment
        SortDialogFragment fragment = new SortDialogFragment();
        // Add the parameters to the Fragment
        Bundle args = new Bundle();
        args.putParcelable("params", params);
        fragment.setArguments(args);

        return fragment;
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
            this.mRequestParams = args.getParcelable("params");
        }
    }

    /**
     * onCreateDialog
     *
     * @param savedInstanceState
     * @return The instantiated dialog
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        // Inflater
        LayoutInflater inflater = this.getActivity().getLayoutInflater();
        // Inflate the layout View
        View view = inflater.inflate(R.layout.fragment_sort, null);
        // Set the view
        builder.setView(view);

        // Set up the inputs
        this.setupInputs(view, this.mRequestParams);
        // Set confirmation and cancel buttons
        this.setConfirmationButtons(builder);

        return builder.create();
    }

    /**
     * onAttach
     *
     * @param activity The Activity the Fragment is being attached to
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Make sure the Activity implements this Fragment's listener interface
        try {
            this.mListener = (SortDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(
                    activity.toString() + " does not implement SortDialogListener");
        }
    }

    /**
     * Builds a CapsuleRequestParameters based on the current state of the inputs
     *
     * @return CapsuleRequestParameters based on the current state of the inputs
     */
    public CapsuleRequestParameters buildSortParameters() {
        // Get the search text
        String searchText = this.mSearchEditText.getText().toString();
        // Get the sort parameter value
        int sortParamValue;
        try {
            int selectedItemPosition = this.mSortSpinner.getSelectedItemPosition();
            String sortValueString = this.mSortValues.get(selectedItemPosition);
            sortParamValue = Integer.parseInt(sortValueString);
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            sortParamValue = RequestContract.Value.CAPSULE_SORT_NAME_ASC;
        }

        // Build the request parameters
        return new CapsuleRequestParameters().setPage(1).setSearch(searchText)
                .setSort(sortParamValue);
    }

    /**
     * Sets up the inputs
     *
     * @param layout The layout View
     * @param params The initial parameters used to determine the initial states of the inputs
     */
    private void setupInputs(View layout, CapsuleRequestParameters params) {
        // Get references to the inputs
        this.mSearchEditText = (EditText) layout.findViewById(R.id.fragment_sort_search);
        this.mSortSpinner = (Spinner) layout.findViewById(R.id.fragment_sort_sort_spinner);
        Button resetButton = (Button) layout.findViewById(R.id.fragment_sort_reset);

        // Populate the sort spinner with options
        this.populateSortSpinner(this.mSortSpinner);

        // Set the initial states of the inputs
        this.setInitialInputStates(params);

        // Set the reset button listener
        this.setResetButtonListener(resetButton);
    }

    /**
     * Sets the initial states of the inputs
     *
     * @param params The initial request parameter values
     */
    private void setInitialInputStates(CapsuleRequestParameters params) {
        if (params == null) {
            return;
        }

        // Populate the search field with the string from the parameters
        if (params.getSearch() != null) {
            this.mSearchEditText.setText(params.getSearch().trim());
        }
        // Select the sort option defined in the parameters
        if (this.mSortSpinnerAdapter != null) {
            // Get the selected sort parameter value
            int sortParamValue = params.getSort();
            // Get the index of the sort value
            int sortValueIndex = this.mSortValues.indexOf(String.valueOf(sortParamValue));
            // Set the index as the selected item since the sort values collection should match the spinner items
            if (sortValueIndex >= 0) {
                this.mSortSpinner.setSelection(sortValueIndex);
            }
        }
    }

    /**
     * Set the confirmation buttons for the dialog
     *
     * @param builder The dialog's builder
     */
    private void setConfirmationButtons(AlertDialog.Builder builder) {
        // Positive button
        builder.setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Delegate the confirmation handling to the listener
                SortDialogFragment.this.mListener.onConfirm(SortDialogFragment.this);
            }
        });
        // Negative button
        builder.setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Delegate the cancel handling to the listener
                SortDialogFragment.this.mListener.onCancel(SortDialogFragment.this);
            }
        });
    }

    /**
     * Populate the sort Spinner with options
     *
     * @param sortSpinner The sort Spinner
     */
    private void populateSortSpinner(Spinner sortSpinner) {
        if (sortSpinner == null) {
            return;
        }

        // Get the sort options and corresponding parameter values from the resources
        String[] sortOptionStrings = this.getResources().getStringArray(R.array.sort_options);
        String[] sortValueStrings = this.getResources().getStringArray(R.array.sort_key_values);
        // Convert them to List collections
        List<String> sortOptions = Arrays.asList(sortOptionStrings);
        this.mSortValues = Arrays.asList(sortValueStrings);
        // Instantiate the Adapter for the sort Spinner
        this.mSortSpinnerAdapter =
                new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item,
                        sortOptions);
        // Set the drop-down View layout
        this.mSortSpinnerAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Set the Adapter on the Spinner
        sortSpinner.setAdapter(this.mSortSpinnerAdapter);
    }

    /**
     * Set up the reset button's listener, which resets all the inputs
     *
     * @param button The reset button
     */
    private void setResetButtonListener(Button button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SortDialogFragment.this.mSearchEditText != null) {
                    SortDialogFragment.this.mSearchEditText.setText("");
                }
                if (SortDialogFragment.this.mSortSpinner != null) {
                    SortDialogFragment.this.mSortSpinner.setSelection(0);
                }
            }
        });
    }

    /**
     * Listener interface for handling the SortDialogFragment's events
     */
    public interface SortDialogListener {

        /**
         * Handles the event of clicking the SortDialogFragment's positive button
         *
         * @param fragment A reference to the SortDialogFragment
         */
        void onConfirm(SortDialogFragment fragment);

        /**
         * Handles the event of clicking the SortDialogFragment's negative button
         *
         * @param fragment A reference to the SortDialogFragment
         */
        void onCancel(SortDialogFragment fragment);

    }

}
