package edu.stanford.braincat.rulepedia.events;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;

/**
 * Created by gcampagn on 5/26/15.
 */
public abstract class MessengerEventSource implements EventSource {
    private volatile IBinder service;
    private ServiceConnection connection;
    private Messenger messenger;

    private static class MessageHandler extends EventSourceHandler {
        private final MessengerEventSource source;
        private final EventSourceHandler chained;

        public MessageHandler(Looper looper, MessengerEventSource source, EventSourceHandler chained) {
            super(looper);
            this.source = source;
            this.chained = chained;
        }

        public void handleMessage(@NonNull Message message) {
            source.handleMessage(message);
        }

        public void messageReceived() {
            chained.messageReceived();
        }

        @Override
        protected void finalize() throws Throwable {
            Log.e("rulepedia.Channels", "MessengerHandler finalized!");
            super.finalize();
        }
    }

    private class MessengerServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            service = binder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            service = null;
        }
    }

    public IBinder getService() {
        return service;
    }

    @Override
    protected void finalize() throws Throwable {
        Log.e("rulepedia.Channels", "MessengerEventSource finalized!");
        super.finalize();
    }

    @Override
    public void install(Context ctx, EventSourceHandler handler) throws IOException {
        MessageHandler self = new MessageHandler(handler.getLooper(), this, handler);
        connection = new MessengerServiceConnection();
        messenger = new Messenger(self);
        ctx.bindService(createIntent(messenger), connection, Context.BIND_AUTO_CREATE);
    }

    protected abstract Intent createIntent(Messenger messenger);

    protected abstract void handleMessage(Message message);

    @Override
    public void uninstall(Context ctx) throws IOException {
        ctx.unbindService(connection);
        connection = null;
    }
}
