package edu.stanford.braincat.rulepedia.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import edu.stanford.braincat.rulepedia.omletUI.OmletUIService;

/**
 * Created by gcampagn on 5/1/15.
 * <p>
 * Based on http://www.jjoe64.com/2011/06/autostart-service-on-device-boot.html
 */
public class AutoStarter extends BroadcastReceiver {
    public static void startExecutorService(Context context) {
        Log.i(RuleExecutorService.LOG_TAG, "Auto starting service");

        Intent pushIntent = new Intent(context, RuleExecutorService.class);
        context.startService(pushIntent);
    }

    public static void startOmletService(Context context) {
        Intent pushIntent = new Intent(context, OmletUIService.class);
        context.startService(pushIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case "android.intent.action.BOOT_COMPLETED":
                startExecutorService(context);
                startOmletService(context);
                break;
            default:
                break;
        }
    }
}
