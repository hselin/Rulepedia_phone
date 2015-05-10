package edu.stanford.braincat.rulepedia.service;

import android.content.Context;
import android.os.Looper;

import org.json.JSONObject;

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

    @Override
    public void run() {
        Looper.prepare();
        executor = new RuleExecutor(context, Looper.myLooper());

        synchronized (this) {
            looper = Looper.myLooper();
            notify();
        }

        executor.prepare();
        Looper.loop();
        executor.destroy();
    }

    public void installRule(JSONObject jsonRule, Callback callback) {
        executor.installRule(jsonRule, callback);
    }

    public void reloadRule(String id, Callback callback) {
        executor.reloadRule(id, callback);
    }
}
