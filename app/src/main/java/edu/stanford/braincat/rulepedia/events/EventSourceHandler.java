package edu.stanford.braincat.rulepedia.events;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;

/**
 * Created by gcampagn on 5/26/15.
 */
public abstract class EventSourceHandler extends Handler {
    public EventSourceHandler(Looper looper) {
        super(looper);
    }

    public EventSourceHandler() {
        super();
    }

    @Override
    public void dispatchMessage(@NonNull Message message) {
        super.dispatchMessage(message);
        messageReceived();
    }

    public abstract void messageReceived();
}
