package edu.stanford.braincat.rulepedia.service;

import android.content.Context;
import android.os.Looper;

/**
 * Created by gcampagn on 4/30/15.
 */
public class RuleExecutorThread extends Thread {
    private final Context context;
    private RuleExecutor executor;
    private Looper looper;

    public RuleExecutorThread(Context ctx)  {
        context = ctx;
    }

    public synchronized Looper getLooper() throws InterruptedException {
        while (looper == null)
            wait();
        return looper;
    }

    public synchronized RuleExecutor getExecutor() throws InterruptedException {
        while (executor == null)
            wait();
        return executor;
    }

    @Override
    public void run() {
        Looper.prepare();

        synchronized (this) {
            executor = new RuleExecutor(context, Looper.myLooper());
            looper = Looper.myLooper();
            notifyAll();
        }

        executor.prepare();
        Looper.loop();
        executor.destroy();
    }
}
