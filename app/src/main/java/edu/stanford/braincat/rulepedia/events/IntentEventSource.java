package edu.stanford.braincat.rulepedia.events;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Created by gcampagn on 5/2/15.
 */
public class IntentEventSource implements EventSource {
    private final IntentFilter filter;
    private final Queue<Intent> queue;
    private BroadcastReceiver receiver;

    public class EventSourceBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context ctx, Intent intent) {
            queue.offer(intent);
        }
    }

    public IntentEventSource(IntentFilter filter) {
        this.filter = filter;
        this.queue = new ArrayDeque<>();
    }

    public Intent getLastIntent() {
        return queue.element();
    }

    @Override
    public void install(Context ctx, EventSourceHandler handler) throws IOException {
        if (receiver != null)
            throw new IllegalStateException("double install");
        receiver = new EventSourceBroadcastReceiver();
        ctx.registerReceiver(receiver, filter, null, handler);
    }

    @Override
    public void uninstall(Context ctx) throws IOException {
        if (receiver == null)
            throw new IllegalStateException("double uninstall");
        ctx.unregisterReceiver(receiver);
        receiver = null;
    }

    @Override
    public boolean checkEvent() {
        return !queue.isEmpty();
    }

    @Override
    public void updateState() {
        queue.poll();
    }
}
