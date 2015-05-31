package edu.stanford.braincat.rulepedia.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import edu.stanford.braincat.rulepedia.service.RuleExecutor;
import edu.stanford.braincat.rulepedia.service.RuleExecutorService;

/**
 * Created by gcampagn on 5/30/15.
 */
public class RuleExecutorConnection implements ServiceConnection {
    private final Context parentContext;
    private RuleExecutor executor;

    public RuleExecutorConnection(Context parent) {
        parentContext = parent;
    }

    public RuleExecutor getExecutor() {
        return executor;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        executor = ((RuleExecutorService.Binder) iBinder).getRuleExecutor();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        executor = null;
    }

    public void start() {
        Intent intent = new Intent(parentContext, RuleExecutorService.class);
        parentContext.bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    public void stop() {
        parentContext.unbindService(this);
    }
}
