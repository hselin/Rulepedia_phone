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
    public static final String NOTIFY_USER_NEW_DEVICE_DETECTED = "edu.stanford.braincat.rulepedia.omlet.NOTIFY_USER_NEW_DEVICE_DETECTED";
    public static final String SAY_RANDOM_QUOTES = "edu.stanford.braincat.rulepedia.omlet.SAY_RANDOM_QUOTES";


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
        if (intent == null || intent.getAction() == null)
            return Service.START_REDELIVER_INTENT;

        if (!ensureWebHook()) {
            stopSelf(startId);
            return Service.START_REDELIVER_INTENT;
        }

        switch (intent.getAction()) {
            case WELCOME_USER:
                doWelcomeUser();
                break;

            case NOTIFY_USER_NEW_DEVICE_DETECTED:
                String uuid = intent.getStringExtra("UUID");
                String deviceType = intent.getStringExtra("TYPE");

                doNotifyUserNewDeviceDetected(uuid, deviceType);
                break;
            case SAY_RANDOM_QUOTES:
                String quote = intent.getStringExtra("QUOTE");
                doSayRandomQuote(quote);
                break;

            default:
                break;
        }

        return Service.START_REDELIVER_INTENT;
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

    private void doNotifyUserNewDeviceDetected(String uuid, String deviceType)
    {
        String rulepediaURL = "https://vast-hamlet-6003.herokuapp.com/query/" + deviceType;
        sendMessage("Hello! I've detected a " + deviceType + " device with UUID " + uuid + "\n" +
        "here is a list of spells that we could use with it " + rulepediaURL);
    }

    private void doSayRandomQuote(String quote)
    {
        sendMessage(quote);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
