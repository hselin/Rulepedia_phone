package edu.stanford.braincat.rulepedia.omletUI;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import edu.stanford.braincat.rulepedia.channels.HTTPUtil;

public class OmletUIService extends Service {
    public static final String WELCOME_USER = "edu.stanford.braincat.rulepedia.omlet.WELCOME_USER";

    public static final String LOG_TAG = "rulepedia.OmletUI";

    private String webHook;

    public OmletUIService() {
    }

    private boolean ensureWebHook() {
        if (webHook != null)
            return true;

        try {
            webHook = getSharedPreferences("omlet", MODE_PRIVATE).getString("webhook", null);
            if (webHook == null)
                return false;
            new URL(webHook);
            return true;
        } catch(MalformedURLException e) {
            webHook = null;
            return false;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null)
            return Service.START_NOT_STICKY;

        if (!ensureWebHook()) {
            stopSelf();
            return Service.START_STICKY;
        }

        switch (intent.getAction()) {
            case WELCOME_USER:
                doWelcomeUser();
                break;

            default:
                break;
        }

        return Service.START_STICKY;
    }

    private void sendMessage(final String message) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    HTTPUtil.postString(webHook, message);
                } catch(IOException e) {
                    Log.e(LOG_TAG, "Failed to send message to Omlet!", e);
                }
            }
        });
    }

    private void doWelcomeUser() {
        sendMessage("Hello! My name is Sabrina, and I'm ready to use my magic power to help you!");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
