package com.brettnamba.capsules.authenticator;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.brettnamba.capsules.Constants;
import com.brettnamba.capsules.MainActivity;
import com.brettnamba.capsules.R;
import com.brettnamba.capsules.http.RequestHandler;
import com.brettnamba.capsules.http.response.JsonResponse;
import com.brettnamba.capsules.os.AsyncListenerTask;
import com.brettnamba.capsules.os.AuthenticationTask;
import com.brettnamba.capsules.os.RetainedTaskFragment;
import com.brettnamba.capsules.util.Widgets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity to handle registering new accounts via the Web API and adding corresponding Accounts
 * to the Android AccountManager
 */
public class RegisterActivity extends FragmentActivity implements AsyncListenerTask.AuthenticationTaskListener {

    /**
     * AccountManager to handle adding Accounts
     */
    private AccountManager mAccountManager;

    /**
     * Fragment to persist the authentication AsyncTask over the Activity's lifecycle since
     * the AsyncTask runs on the background thread
     */
    private RetainedTaskFragment mAuthenticationTaskFragment;

    /**
     * Input for the username
     */
    private EditText mUsernameInput;

    /**
     * Input for the e-mail address
     */
    private EditText mEmailInput;

    /**
     * Input for the password
     */
    private EditText mPasswordInput;

    /**
     * Input for the password confirmation
     */
    private EditText mPasswordConfirmInput;

    /**
     * Displays messages to the user
     */
    private TextView mMessageView;

    /**
     * The progress indicator to be displayed
     */
    private ProgressDialog mProgressDialog;

    /**
     * onCreate
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_register);

        // AccountManager
        this.mAccountManager = AccountManager.get(this);

        // Get the Fragment to retain the background thread task for authenticating
        this.mAuthenticationTaskFragment =
                RetainedTaskFragment.findOrCreate(this.getSupportFragmentManager());

        // Setup the Toolbar
        Toolbar toolbar = Widgets.createToolbar(this, this.getString(R.string.title_register));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Activity can be triggered either by the app itself or by Android Settings so
                // on an up navigation, finish the Activity
                RegisterActivity.this.finish();
            }
        });

        // Listener for the register button
        Button button = (Button) this.findViewById(R.id.activity_register_submit);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterActivity.this.handleRegistration();
            }
        });

        // Instantiate a new ProgressDialog
        this.mProgressDialog = new ProgressDialog(this);
        this.mProgressDialog.setMessage(this.getString(R.string.progress_please_wait));

        // Keep references to the TextViews so findViewById() doesn't need to be called on multiple registration attempts
        this.mUsernameInput = (EditText) this.findViewById(R.id.activity_register_username);
        this.mEmailInput = (EditText) this.findViewById(R.id.activity_register_email);
        this.mPasswordInput = (EditText) this.findViewById(R.id.activity_register_password);
        this.mPasswordConfirmInput = (EditText) this.findViewById(R.id.activity_register_confirm_password);
        this.mMessageView = (TextView) this.findViewById(R.id.messages);
    }

    /**
     * Handles clicking on the "register" button
     */
    private void handleRegistration() {
        // Get the values from the inputs
        final String username = this.mUsernameInput.getText().toString();
        final String email = this.mEmailInput.getText().toString();
        final String password = this.mPasswordInput.getText().toString();
        final String passwordConfirm = this.mPasswordConfirmInput.getText().toString();
        // Validate the inputs
        if (this.isValid(username, email, password, passwordConfirm)) {
            // No errors so send an HTTP authentication request to the API on the background thread
            AuthenticationTask authenticationTask = new AuthenticationTask(this);
            // Retain the AsyncTask in a RetainedTaskFragment
            this.mAuthenticationTaskFragment.setTask(authenticationTask);
            // Execute
            authenticationTask.execute(username, email, password, passwordConfirm);
        }
    }

    /**
     * Handles the registration response by checking whether an authentication token
     * was received.  If one was received, it will add the new Account to the device.
     *
     * @param response Represents the response of the registration request
     */
    private void finishRegistration(JsonResponse response) {
        // Check the response for an authentication token
        if (response.getAuthToken() != null && response.getAuthToken().length() > 0) {
            // Get the username
            final String username = this.mUsernameInput.getText().toString();
            // Add the Account to the device and associate the authentication token to the new Account
            final Account account = new Account(username, Constants.ACCOUNT_TYPE);
            this.mAccountManager.addAccountExplicitly(account, null, null);
            this.mAccountManager.setAuthToken(account, Constants.AUTH_TOKEN_TYPE, response.getAuthToken());
            // Start the MainActivity as the beginning of the stack and finish
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(intent);
            this.finish();
        } else {
            // There was no authentication token
            List<String> errors = new ArrayList<String>();
            errors.add(this.getString(R.string.error_missing_auth_token));
            this.setMessages(errors);
        }
    }

    /**
     * Validates the inputs
     *
     * @param username        The value from the username input
     * @param email           The value from the email input
     * @param password        The value from the password input
     * @param passwordConfirm The value from the password confirmation input
     * @return boolean True if all the values are valid, false if they are not
     */
    private boolean isValid(String username, String email, String password, String passwordConfirm) {
        // Will hold any errors from validation
        List<String> errors = new ArrayList<String>();

        // Username
        if (TextUtils.isEmpty(username)) {
            errors.add(this.getString(R.string.validation_username));
        }

        // Email
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errors.add(this.getString(R.string.validation_email));
        }

        // Password
        if (TextUtils.isEmpty(password)) {
            errors.add(this.getString(R.string.validation_password));
        }

        // Password confirmation
        if (TextUtils.isEmpty(passwordConfirm)) {
            errors.add(this.getString(R.string.validation_password_confirmation));
        }

        // Passwords match
        if (!password.equals(passwordConfirm)) {
            errors.add(this.getString(R.string.validation_password_match));
        }

        boolean isValid = errors.isEmpty();
        if (!isValid) {
            this.setMessages(errors);
        }
        return isValid;
    }

    /**
     * Sets the collection of messages and displays them to the user
     *
     * @param messages Collection of messages to be displayed
     */
    private void setMessages(List<String> messages) {
        if (this.mMessageView != null) {
            this.mMessageView.setText(TextUtils.join("\n", messages));
            this.mMessageView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Clears any messages and hides the View
     */
    private void clearMessages() {
        if (this.mMessageView != null) {
            this.mMessageView.setVisibility(View.GONE);
            this.mMessageView.setText("");
        }
    }

    /**
     * Callback for the authentication AsyncTask's doInBackground()
     *
     * @param params Credentials to be used in the authentication HTTP request
     * @return JsonResponse Response of the authentication request
     */
    @Override
    public JsonResponse duringAuthentication(String... params) {
        try {
            final String username = params[0];
            final String email = params[1];
            final String password = params[2];
            final String passwordConfirm = params[3];
            return RequestHandler.register(this.getApplicationContext(), username, email, password,
                    passwordConfirm);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Callback for the authentication AsyncTask's onPreExecute()
     */
    @Override
    public void onPreAuthentication() {
        // Clear any old messages
        this.clearMessages();
        // Show the progress indicator
        if (this.mAuthenticationTaskFragment != null
                && this.mAuthenticationTaskFragment.isTaskRunning()) {
            this.mProgressDialog.show();
        }
    }

    /**
     * Callback for the authentication AsyncTask's onPostExecute()
     *
     * @param response The HTTP response from the authentication request
     */
    @Override
    public void onPostAuthentication(JsonResponse response) {
        if (response != null) {
            if (response.isError()) {
                // Set messages
                this.setMessages(response.getMessages());
            } else {
                this.finishRegistration(response);
            }
        }
        // Hide the progress dialog
        this.mProgressDialog.hide();
    }

    /**
     * Callback for the authentication AsyncTask's onCancelled()
     */
    @Override
    public void onAuthenticationCancelled() {
        // Hide the progress dialog
        this.mProgressDialog.hide();
    }

}
