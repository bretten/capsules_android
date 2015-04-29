package com.brettnamba.capsules.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.brettnamba.capsules.Constants;

/**
 * Utility class for handling Accounts
 */
public class Accounts {

    private static final String SHARED_PREFS_SETTINGS_KEY = "settings";
    private static final String LAST_USED_ACCOUNT_KEY = "last_used_account_name";

    /**
     * Gets the index of an Account name given a collection
     *
     * @param accounts Collection of Accounts
     * @param accountName The Account name
     * @return int The index of the account or -1 if it does not exist
     */
    public static int indexOf(Account[] accounts, String accountName) {
        // Iterate through all the Accounts to see if a match is found
        if (accounts.length > 0) {
            for (int i = 0; i < accounts.length; i++) {
                if (accounts[i].name.equals(accountName)) {
                    return i;
                }
            }
        }
        // The Account was not found or the collection was empty
        return -1;
    }

    /**
     * Sets the Account as being the one that was last used
     *
     * @param activity The Activity for accessing SharedPreferences
     * @param account The Account to switch to
     */
    public static void setLastUsedAccount(Activity activity, Account account) {
        SharedPreferences sharedPrefs = activity.getSharedPreferences(SHARED_PREFS_SETTINGS_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(LAST_USED_ACCOUNT_KEY, account.name);
        editor.apply();
    }

    /**
     * Gets the last used Account from SharedPreferences.  If one could not be found, it will default
     * to the first Account on the device.  If no Accounts exist, null will be returned.
     *
     * @param activity The Activity for accessing SharedPreferences
     * @return The last used or default Account, or null if no Accounts are on the device
     */
    public static Account getLastUsedOrFirstAccount(Activity activity) {
        // Get the AccountManager
        AccountManager accountManager = AccountManager.get(activity);
        // Get all the Accounts since AccountManager cannot get individual ones
        Account[] deviceAccounts = accountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
        if (deviceAccounts.length > 0) {
            // Get the last used Account name out of SharedPreferences
            SharedPreferences sharedPrefs = activity.getSharedPreferences(SHARED_PREFS_SETTINGS_KEY, Context.MODE_PRIVATE);
            String lastUsedAccountName = sharedPrefs.getString(LAST_USED_ACCOUNT_KEY, null);
            // See if the last used Account is still on the device
            int index = Accounts.indexOf(deviceAccounts, lastUsedAccountName);
            if (index >= 0) {
                // The last used Account was found
                return deviceAccounts[index];
            } else {
                // The last used Account was not found, so return the first Account
                return deviceAccounts[0];
            }
        } else {
            // There are no Accounts on the device
            return null;
        }
    }

}
