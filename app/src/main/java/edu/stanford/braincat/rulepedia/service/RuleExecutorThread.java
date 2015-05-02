package edu.stanford.braincat.rulepedia.service;

import android.content.Context;
import android.os.Looper;

import edu.stanford.braincat.rulepedia.model.RuleDatabase;

/**
 * Created by gcampagn on 4/30/15.
 */
public class RuleExecutorThread extends Thread {
    private final Context context;
    private final RuleDatabase database;
    private RuleExecutor executor;
    private Looper looper;

    public RuleExecutorThread(Context ctx, RuleDatabase db)  {
        context = ctx;
        database = db;
    }

    public synchronized Looper getLooper() throws InterruptedException {
        while (looper == null)
            wait();
        return looper;
    }

    @Override
    public void run() {
        Looper.prepare();
        executor = new RuleExecutor(context, Looper.myLooper(), database);

        synchronized (this) {
            looper = Looper.myLooper();
            notify();
        }

        Looper.loop();
    }
}
