package edu.stanford.braincat.rulepedia.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import edu.stanford.braincat.rulepedia.exceptions.DuplicatedRuleException;
import edu.stanford.braincat.rulepedia.model.Rule;
import edu.stanford.braincat.rulepedia.service.Callback;
import edu.stanford.braincat.rulepedia.service.RuleExecutor;

/**
 * Created by gcampagn on 5/30/15.
 */
public class RuleInstaller {
    private static void onRuleInstalled(final Activity parentActivity) {
        new AlertDialog.Builder(parentActivity)
                .setTitle("Success")
                .setMessage("I learned this magic spell, and I'm ready to use it!")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        parentActivity.startActivityForResult(new Intent(parentActivity, GoogleFitAuthActivity.class), 0);

                        // continue with delete
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    private static void onRuleInstallationError(final Activity parentActivity, Exception error) {
        if(error instanceof DuplicatedRuleException) {
            new AlertDialog.Builder(parentActivity)
                    .setTitle("Error")
                    .setMessage("Magic spell already in the book")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else {
            new AlertDialog.Builder(parentActivity)
                    .setTitle("Sorry, that did not work")
                    .setMessage("Internal error " + error.toString())
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    private static void doInstallRule(final MainActivity parentActivity, final JSONObject jsonRule) {
        RuleExecutor executor = parentActivity.getRuleExecutor();
        if (executor == null)
            return;

        executor.installRule(jsonRule, new Callback<Rule>() {
            @Override
            public void run(final Rule result, final Exception error) {
                parentActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        if (result != null) {
                            onRuleInstalled(parentActivity);
                        } else {
                            onRuleInstallationError(parentActivity, error);
                        }
                    }
                });
            }
        });
    }

    public static void installRule(final MainActivity parentActivity, final String json) {
        try {
            JSONObject jsonRule = (JSONObject) new JSONTokener(json).nextValue();
            doInstallRule(parentActivity, jsonRule);
        } catch (JSONException | ClassCastException e) {
            Log.e(MainActivity.LOG_TAG, "Failed to parse rule JSON: " + e.getMessage());
            onRuleInstallationError(parentActivity, e);
        }
    }

    public static void installRuleConfirm(final MainActivity parentActivity, final String json) throws JSONException {
        try {
            final JSONObject jsonRule = (JSONObject) new JSONTokener(json).nextValue();
            new AlertDialog.Builder(parentActivity)
                    .setTitle("Enable magic spell")
                    .setMessage("Do you want me to use the spell " + jsonRule.getString("name") + "?\n" +
                            "The description says: " + jsonRule.getString("description"))
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            doInstallRule(parentActivity, jsonRule);
                            // continue with delete
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // continue with delete
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } catch (JSONException | ClassCastException e) {
            Log.e(MainActivity.LOG_TAG, "Failed to parse rule JSON: " + e.getMessage());
            onRuleInstallationError(parentActivity, e);
        }
    }
}
