package edu.stanford.braincat.rulepedia.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import edu.stanford.braincat.rulepedia.model.RuleDatabase;

public class RuleExecutorService extends Service {
    private RuleExecutorThread executorThread;
    private Looper executorLooper;
    private RuleDatabase database;

    public static final String LOG_TAG = "rulepedia.Service";

    public RuleExecutorService() {
    }

    @Override
    public void onCreate() {
        Log.i(LOG_TAG, "Creating service...");

        try {
            database = new RuleDatabase();
            database.loadForExecution(this);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load database: " + e.getMessage());
            stopSelf();
        }

        Log.i(LOG_TAG, "Created service");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOG_TAG, "Starting service...");

        if (executorThread != null)
            throw new IllegalStateException("Executor thread is already running");

        executorThread = new RuleExecutorThread(this, database);
        executorThread.start();

        while (executorLooper == null) {
            try {
                executorLooper = executorThread.getLooper();
            } catch (InterruptedException ie) {
                Log.w(LOG_TAG, "Interrupted exception while starting service!");
                // not much we can do, let's try again...
            }
        }

        Log.i(LOG_TAG, "Started service");

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

        Log.i(LOG_TAG, "Destroyed service");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
