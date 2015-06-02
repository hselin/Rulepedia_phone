package edu.stanford.braincat.rulepedia.channels.generic;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import org.mozilla.javascript.ScriptableObject;

import java.io.IOException;

import edu.stanford.braincat.rulepedia.channels.ServiceBinder;
import edu.stanford.braincat.rulepedia.events.EventSourceHandler;

/**
 * Created by gcampagn on 6/1/15.
 */
public abstract class GenericChannelPlugin {
    private final ServiceBinder binder;

    public GenericChannelPlugin(Intent intent) {
        binder = new ServiceBinder(intent);
    }

    public void enable(Context ctx, EventSourceHandler handler) throws IOException {
        binder.enable(ctx, handler);
    }

    public abstract void update(Context ctx, ScriptableObject self);

    public void disable(Context ctx) throws IOException {
        binder.disable(ctx);
    }

    public IBinder getService() {
        return binder.getService();
    }
}
