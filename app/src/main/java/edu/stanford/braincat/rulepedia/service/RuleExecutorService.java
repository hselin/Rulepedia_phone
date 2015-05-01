package edu.stanford.braincat.rulepedia.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

import edu.stanford.braincat.rulepedia.events.CrossThreadIdleEventSource;
import edu.stanford.braincat.rulepedia.model.RuleDatabase;

public class RuleExecutorService extends Service {
    private Thread executorThread;
    private CrossThreadIdleEventSource terminationSource;
    private RuleDatabase database;

    public static final String LOG_TAG = "rulepedia.Service";

    public RuleExecutorService() {
    }

    @Override
    public void onCreate() {
        try {
            database = new RuleDatabase();
            database.load();
        } catch(IOException e) {
            Log.e(LOG_TAG, "Failed to load database: " + e.getMessage());
            stopSelf();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (executorThread != null)
            throw new IllegalStateException("Executor thread is already running");

        terminationSource = new CrossThreadIdleEventSource();
        try {
            executorThread = new RuleExecutorThread(this, database, terminationSource);
            executorThread.start();

            // We're a background service and we expect to be running
            // most of the time, so ask the system to keep us alive if
            // memory is not a problem
            return START_STICKY;
        } catch(IOException e) {
            Log.e(LOG_TAG, "Failed to start execution thread: " + e.getMessage());
            stopSelf();
            return START_STICKY;
        }
    }

    @Override
    public void onDestroy() {
        terminationSource.fireEvent(null);
        while (executorThread.isAlive()) {
            try {
                executorThread.join();
            } catch (InterruptedException e) {
            }
        }
        executorThread = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
