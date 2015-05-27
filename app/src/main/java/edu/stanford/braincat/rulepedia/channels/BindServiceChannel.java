package edu.stanford.braincat.rulepedia.channels;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import edu.stanford.braincat.rulepedia.events.EventSourceHandler;
import edu.stanford.braincat.rulepedia.model.Channel;
import edu.stanford.braincat.rulepedia.model.ChannelFactory;

/**
 * Created by gcampagn on 5/26/15.
 */
public abstract class BindServiceChannel extends Channel implements ServiceConnection {
    private volatile IBinder service;
    private ServiceConnection connection;

    public BindServiceChannel(ChannelFactory factory, String url) {
        super(factory, url);
    }

    public IBinder getService() {
        return service;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder binder) {
        service = binder;
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        service = null;
    }

    protected abstract Intent createIntent();

    @Override
    public void enable(Context ctx, EventSourceHandler handler) {
        ctx.bindService(createIntent(), connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void disable(Context ctx) {
        ctx.unbindService(connection);
        connection = null;
    }
}
