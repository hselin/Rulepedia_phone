package edu.stanford.braincat.rulepedia.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import edu.stanford.braincat.rulepedia.omletUI.OmletUIService;
import mobisocial.osm.IOsmService;

/**
 * Created by gcampagn on 5/30/15.
 */
public class OmletServiceConnection implements ServiceConnection {
    private final Context parentContext;
    private IOsmService omletService;
    private boolean needsInstall;

    public OmletServiceConnection(Context parent) {
        parentContext = parent;
    }

    public void start() {
        Intent intent = new Intent("mobisocial.intent.action.BIND_SERVICE");
        intent.setPackage("mobisocial.omlet");
        parentContext.bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    public void stop() {
        parentContext.unbindService(this);
    }

    public void ensureOmlet() {
        SharedPreferences prefs = parentContext.getSharedPreferences("omlet", Context.MODE_PRIVATE);
        String sabrinaFeed = prefs.getString("feedUri", null);
        needsInstall = sabrinaFeed == null;

        if (needsInstall)
            startOmletWebApp();
        else
            Log.i(MainActivity.LOG_TAG, "Sabrina feed is already created with URI " + sabrinaFeed);

        if (prefs.getString("webhook", null) != null)
            Log.i(MainActivity.LOG_TAG, "Sabrina webhook is already configured at " + prefs.getString("webhook", null));
    }

    public void setWebhook(String webhook) {
        SharedPreferences prefs = parentContext.getSharedPreferences("omlet", Context.MODE_PRIVATE);
        if (webhook.equals(prefs.getString("webhook", null))) {
            return;
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("webhook", webhook);
        editor.apply();

        Intent intent = new Intent(parentContext, OmletUIService.class);
        intent.setAction(OmletUIService.WELCOME_USER);
        parentContext.startService(intent);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        omletService = IOsmService.Stub.asInterface(iBinder);
        if (needsInstall)
            startOmletWebApp();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        omletService = null;
    }

    private void startOmletWebApp() {
        if (omletService == null)
            return;

        try {
            Uri sabrinaFeed = omletService.createFeed("Sabrina", Uri.parse("https://vast-hamlet-6003.herokuapp.com/images/world3.jpg"), new long[]{});

            try {
                JSONObject json = new JSONObject();

                json.put("noun", "invitation");
                json.put("displayTitle", "Sabrina");
                json.put("displayThumbnailUrl", "https://vast-hamlet-6003.herokuapp.com/images/world3.jpg");
                json.put("displayText", "Click here to install Sabrina!");
                json.put("json", "true");
                json.put("callback", "https://vast-hamlet-6003.herokuapp.com/webhook/install");

                omletService.sendObj(sabrinaFeed, "rdl", json.toString());
            } catch (JSONException je) {
                throw new RuntimeException(je);
            }

            SharedPreferences.Editor editor = parentContext.getSharedPreferences("omlet", Context.MODE_PRIVATE).edit();
            editor.putString("feedUri", sabrinaFeed.toString());
            editor.apply();
            needsInstall = false;

            Intent viewIntent = new Intent(Intent.ACTION_VIEW);
            viewIntent.setPackage("mobisocial.omlet");
            viewIntent.setDataAndType(sabrinaFeed, "vnd.mobisocial/group");
            parentContext.startActivity(viewIntent);
        } catch(RemoteException e) {
            Log.e(MainActivity.LOG_TAG, "Failed to tell Omlet to show the Sabrina installation UI!", e);
        }
    }
}
