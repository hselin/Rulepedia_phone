package edu.stanford.braincat.rulepedia.service;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;

/**
 * Created by gcampagn on 4/30/15.
 */
public class RuleExecutorThread extends Thread {
    private final Context context;
    private boolean started;
    private RuleExecutor executor;
    private Looper looper;

    public RuleExecutorThread(Context ctx) {
        context = ctx;
    }

    public synchronized Looper getLooper() throws InterruptedException {
        while (!started)
            wait();
        return looper;
    }

    public synchronized RuleExecutor getExecutor() throws InterruptedException {
        while (!started)
            wait();
        return executor;
    }

    @Override
    public void run() {
        Looper.prepare();

        synchronized (this) {
            executor = new RuleExecutor(context, Looper.myLooper());
            looper = Looper.myLooper();
            started = true;
            notifyAll();
        }

        executor.prepare();
        Looper.loop();
        executor.destroy();
        try {
            executor.save();
        } catch (IOException e) {
            Log.e(RuleExecutorService.LOG_TAG, "Failed to save database to disk", e);
        }
    }
}
