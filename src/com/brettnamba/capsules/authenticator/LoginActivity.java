package com.brettnamba.capsules.authenticator;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.brettnamba.capsules.Constants;
import com.brettnamba.capsules.R;
import com.brettnamba.capsules.http.HttpFactory;
import com.brettnamba.capsules.http.RequestHandler;
import com.brettnamba.capsules.http.response.AuthenticationResponse;
import com.brettnamba.capsules.os.AsyncListenerTask;
import com.brettnamba.capsules.os.AuthenticationTask;
import com.brettnamba.capsules.os.RetainedTaskFragment;

import org.apache.http.HttpResponse;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity to handle authenticating users against the Web API and adding corresponding
 * Accounts to the Android AccountManager
 */
public class LoginActivity extends FragmentActivity implements AsyncListenerTask.AuthenticationTaskListener {

    /**
     * Handles adding Accounts to the device
     */
    private AccountManager mAccountManager;

    /**
     * Handles HTTP requests
     */
    private RequestHandler mRequestHandler;

    /**
     * Fragment to persist an authentication AsyncTask over the Activity's lifecycle
     * since the AsyncTask runs on the background thread
     */
    private RetainedTaskFragment mAuthenticationTaskFragment;

    /**
     * Authentication response for the AccountManager
     */
    private AccountAuthenticatorResponse mAccountAuthenticatorResponse;

    /**
     * Result Bundle for an authentication response
     */
    private Bundle mResultBundle;

    /**
     * Displays any messages to the user
     */
    private TextView mMessageView;

    /**
     * Input for the username
     */
    private EditText mUsernameInput;

    /**
     * Input for the password
     */
    private EditText mPasswordInput;

    /**
     * Tag for referencing the Fragment that persists the authentication AsyncTask
     */
    private static final String AUTHENTICATION_TASK_FRAGMENT_TAG = "auth_task";

    /**
     * onCreate
     *
     * @param icicle
     */
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.setContentView(R.layout.activity_login);

        // Check to see if an AccountAuthenticatorResponse was passed with the Intent
        this.mAccountAuthenticatorResponse = this.getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);
        if (this.mAccountAuthenticatorResponse != null) {
            this.mAccountAuthenticatorResponse.onRequestContinued();
        }

        // AccountManager
        this.mAccountManager = AccountManager.get(this);
        // HTTP request handler
        this.mRequestHandler = new RequestHandler(HttpFactory.getInstance());

        // If the Activity is being recreated, see if there are any retained Fragments
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        this.mAuthenticationTaskFragment = ((RetainedTaskFragment) fragmentManager.findFragmentByTag(AUTHENTICATION_TASK_FRAGMENT_TAG));
        // If the Activity is being created for the first time, create the RetainedTaskFragments
        if (this.mAuthenticationTaskFragment == null) {
            this.mAuthenticationTaskFragment = new RetainedTaskFragment();
            fragmentManager.beginTransaction().add(this.mAuthenticationTaskFragment, AUTHENTICATION_TASK_FRAGMENT_TAG).commit();
        }

        // Setup the Toolbar
        Toolbar toolbar = (Toolbar) this.findViewById(R.id.toolbar);
        toolbar.setTitle(this.getString(R.string.app_name));
        toolbar.setSubtitle(this.getString(R.string.title_login));
        toolbar.setNavigationIcon(R.drawable.ic_chevron_left_white);
        toolbar.setLogo(R.drawable.ic_launcher);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Activity can be triggered either by the app itself or by Android Settings so
                // on an up navigation, finish the Activity
                LoginActivity.this.finish();
            }
        });

        // Inflate and setup the menu
        toolbar.inflateMenu(R.menu.login);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_register:
                        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                        LoginActivity.this.startActivity(intent);
                        return true;
                    default:
                        return false;
                }
            }
        });

        // Set the listener on the login button
        Button loginButton = (Button) this.findViewById(R.id.activity_login_submit);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginActivity.this.handleLogin();
            }
        });

        // Keep references to the TextViews so findViewById() doesn't need to be called on multiple login attempts
        this.mUsernameInput = (EditText) this.findViewById(R.id.activity_login_username);
        this.mPasswordInput = (EditText) this.findViewById(R.id.activity_login_password);
        this.mMessageView = (TextView) this.findViewById(R.id.messages);
    }

    /**
     * onResume
     *
     * Sets the TaskListener for any retained Fragments that are persisting AsyncTasks
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Update any AsyncTasks contained in RetainedTaskFragments with a reference to the newly created Activity
        if (this.mAuthenticationTaskFragment != null && this.mAuthenticationTaskFragment.getTask() != null) {
            this.mAuthenticationTaskFragment.getTask().setListener(this);
        }
    }

    /**
     * finish
     *
     * Handles the AccountAuthenticatorResponse
     */
    @Override
    public void finish() {
        // Send the result or an error and clear out the response
        if (this.mAccountAuthenticatorResponse != null) {
            if (this.mResultBundle != null) {
                this.mAccountAuthenticatorResponse.onResult(this.mResultBundle);
            } else {
                this.mAccountAuthenticatorResponse.onError(AccountManager.ERROR_CODE_CANCELED, "canceled");
            }
            // Clear out the response
            this.mAccountAuthenticatorResponse = null;
        }
        super.finish();
    }

    /**
     * Sets the result for the request that is seeking an authentication result
     *
     * @param result The result of the authentication
     */
    public final void setAccountAuthenticatorResult(Bundle result) {
        this.mResultBundle = result;
    }

    /**
     * Handles clicks on the "login" button
     */
    private void handleLogin() {
        // Get the values from the inputs
        final String username = this.mUsernameInput.getText().toString();
        final String password = this.mPasswordInput.getText().toString();
        // Make sure the username and password EditTexts are not empty
        if (this.isValid(username, password)) {
            // Authenticate the user credentials on the background thread
            AuthenticationTask authenticationTask = new AuthenticationTask(this);
            // Retain the AsyncTask in the RetainedTaskFragment
            this.mAuthenticationTaskFragment.setTask(authenticationTask);
            // Execute
            authenticationTask.execute(username, password);
        }
    }

    /**
     * Handles the response from an authentication HTTP request
     *
     * @param response HTTP response representing the authentication result
     */
    private void handleAuthenticationResponse(AuthenticationResponse response) {
        // Check if there is an authentication token in the response
        if (response.getAuthToken() != null && response.getAuthToken().length() > 0) {
            // Get the username
            final String username = this.mUsernameInput.getText().toString();
            // Add the Account and associate the authentication token
            final Account account = new Account(username, Constants.ACCOUNT_TYPE);
            this.mAccountManager.addAccountExplicitly(account, null, null);
            this.mAccountManager.setAuthToken(account, Constants.AUTH_TOKEN_TYPE, response.getAuthToken());
            // Finish
            final Intent intent = new Intent();
            intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, username);
            intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
            this.setAccountAuthenticatorResult(intent.getExtras());
            this.setResult(RESULT_OK);
            this.finish();
        } else {
            // There was no authentication token
            List<String> errors = new ArrayList<String>();
            errors.add(this.getString(R.string.error_missing_auth_token));
            this.setMessages(errors);
        }
    }

    /**
     * Validates the user input
     *
     * @param username The value from the username input
     * @param password The value from the password input
     * @return boolean True on valid, false on errors
     */
    private boolean isValid(String username, String password) {
        // Will hold any validation messages
        List<String> errors = new ArrayList<String>();

        // Username
        if (TextUtils.isEmpty(username)) {
            errors.add(this.getString(R.string.validation_username));
        }

        // Password
        if (TextUtils.isEmpty(password)) {
            errors.add(this.getString(R.string.validation_password));
        }

        final boolean isValid = errors.isEmpty();
        if (!isValid) {
            this.setMessages(errors);
        }
        return isValid;
    }

    /**
     * Sets all the messages specified in the collection
     *
     * @param messages Collection of messages to be set
     */
    private void setMessages(List<String> messages) {
        if (this.mMessageView != null) {
            this.mMessageView.setText(TextUtils.join("\n", messages));
            this.mMessageView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Clears all the displayed messages
     */
    private void clearMessages() {
        if (this.mMessageView != null) {
            this.mMessageView.setText("");
            this.mMessageView.setVisibility(View.GONE);
        }
    }

    /**
     * Callback for the authentication AsyncTask's doInBackground()
     *
     * @param params Credentials to be used in the authentication HTTP request
     * @return AuthenticationResponse Response of the authentication request
     */
    @Override
    public AuthenticationResponse duringAuthentication(String... params) {
        try {
            final String username = params[0];
            final String password = params[1];
            HttpResponse httpResponse = this.mRequestHandler.authenticate(username, password);
            return new AuthenticationResponse(httpResponse);
        } catch (IOException|JSONException ex) {
            return null;
        }
    }

    /**
     * Callback for the authentication AsyncTask's onPreExecute()
     */
    @Override
    public void onPreAuthentication() {
        // Clear any messages
        this.clearMessages();
        // Show a progress indicator
        if (this.mAuthenticationTaskFragment != null) {
            this.mAuthenticationTaskFragment.showProgress();
        }
    }

    /**
     * Callback for the authentication AsyncTask's onPostExecute()
     *
     * @param response The HTTP response from the authentication request
     */
    @Override
    public void onPostAuthentication(AuthenticationResponse response) {
        if (response != null) {
            if (response.isClientError() || response.isServerError()) {
                // Show messages
                this.setMessages(response.getMessages());
            } else {
                this.handleAuthenticationResponse(response);
            }
        }
        // Hide the progress indicator
        if (this.mAuthenticationTaskFragment != null) {
            this.mAuthenticationTaskFragment.hideProgress();
        }
    }

    /**
     * Callback for the authentication AsyncTask's onCancelled()
     */
    @Override
    public void onAuthenticationCancelled() {
        // Hide the progress indicator
        if (this.mAuthenticationTaskFragment != null) {
            this.mAuthenticationTaskFragment.hideProgress();
        }
    }

}
