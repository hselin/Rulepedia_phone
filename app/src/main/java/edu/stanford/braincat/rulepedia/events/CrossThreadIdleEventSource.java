package edu.stanford.braincat.rulepedia.events;

import java.io.IOException;
import java.nio.channels.Selector;

/**
 * Created by gcampagn on 4/30/15.
 */
public class CrossThreadIdleEventSource implements EventSource {
    private Thread runningThread;
    private volatile boolean firing;
    private Object payload;

    public void fireEvent(Object payload) {
        this.payload = payload;
        firing = true; // memory barrier
        synchronized (this) {
            if (runningThread != null)
                runningThread.interrupt();
        }
    }

    @Override
    public synchronized void install(Selector sel) throws IOException {
        runningThread = Thread.currentThread();
    }

    @Override
    public synchronized void uninstall() throws IOException {
        runningThread = null;
    }

    @Override
    public long getTimeout() {
        return Long.MAX_VALUE;
    }

    @Override
    public boolean checkEvent() {
        return firing;
    }

    @Override
    public void updateState() {
        firing = false;
    }
}
