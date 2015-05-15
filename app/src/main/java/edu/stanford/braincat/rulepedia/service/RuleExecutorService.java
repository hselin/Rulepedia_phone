package edu.stanford.braincat.rulepedia.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;

import edu.stanford.braincat.rulepedia.model.ObjectDatabase;
import edu.stanford.braincat.rulepedia.model.Rule;
import edu.stanford.braincat.rulepedia.model.RuleDatabase;

public class RuleExecutorService extends Service {
    private RuleExecutorThread executorThread;
    private RuleExecutor executor;
    private Looper executorLooper;
    private ObjectDatabase objectdb;
    private RuleDatabase ruledb;
    private final IBinder binder;

    public static final String INSTALL_RULE_INTENT = "edu.stanford.braincat.rulepedia.INSTALL_RULE";

    public static final String LOG_TAG = "rulepedia.Service";

    public class Binder extends android.os.Binder {
        public RuleExecutor getRuleExecutor() {
            return executor;
        }
    }

    public RuleExecutorService() {
        binder = new Binder();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        Log.i(LOG_TAG, "Creating service...");

        try {
            objectdb = ObjectDatabase.get();
            objectdb.load(this);
            ruledb = RuleDatabase.get();
            ruledb.load(this);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load database: " + e.getMessage());
            stopSelf();
        }

        Log.i(LOG_TAG, "Created service");
    }

    private void doStartService() {
        Log.i(LOG_TAG, "Starting service...");

        executorThread = new RuleExecutorThread(this);
        executorThread.start();

        while (executorLooper == null) {
            try {
                executorLooper = executorThread.getLooper();
            } catch (InterruptedException ie) {
                Log.w(LOG_TAG, "Interrupted exception while starting service!");
                // not much we can do, let's try again...
            }
        }
        while (executor == null) {
            try {
                executor = executorThread.getExecutor();
            } catch (InterruptedException ie) {
                Log.w(LOG_TAG, "Interrupted exception while starting service!");
                // not much we can do, let's try again...
            }
        }

        Log.i(LOG_TAG, "Started service");
    }

    private void doInstallRule(Intent intent) {
        try {
            JSONObject jsonObject;

            if (intent.getData().toString().equals("rulepedia:json")) {
                jsonObject = (JSONObject)new JSONTokener(intent.getStringExtra("json")).nextValue();
                executor.installRule(jsonObject, new Callback<Rule>() {
                    @Override
                    public void run(Rule result, Exception error) {
                        // FIXME
                    }
                });
            } else {
                // FIXME
                throw new UnsupportedOperationException();
            }
        } catch(JSONException e) {
            Log.e(LOG_TAG, "Failed to add rule to the database: " + e.getMessage());
            // FIXME
        }
    }

    private void ensureService() {
        if (executorThread != null)
            return;

        doStartService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action;
        if (intent == null || (action = intent.getAction()) == null)
            return onStartService();

        switch (action) {
            case INSTALL_RULE_INTENT:
                return onInstallRule(intent);
            default:
                throw new UnsupportedOperationException("service cannot handle action " + action);
        }
    }

    private int onInstallRule(Intent intent) {
        ensureService();
        doInstallRule(intent);
        return START_STICKY;
    }

    private int onStartService() {
        ensureService();

        // We're a background service and we expect to be running
        // most of the time, so ask the system to keep us alive if
        // memory is not a problem
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(LOG_TAG, "Destroying service...");

        executorLooper.quit();
        while (executorThread.isAlive()) {
            try {
                executorThread.join();
            } catch (InterruptedException e) {
                Log.w(LOG_TAG, "Interrupted exception while stopping service!");
                // not much we can do, let's try again...
            }
        }
        executorThread = null;

        try {
            objectdb.save(this);
            ruledb.save(this);
        } catch(IOException e) {
            Log.e(LOG_TAG, "Failed to save database to disk: " + e.getMessage());
        }

        Log.i(LOG_TAG, "Destroyed service");
    }
}
