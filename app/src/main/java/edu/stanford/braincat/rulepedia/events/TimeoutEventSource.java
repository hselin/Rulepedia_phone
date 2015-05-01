package edu.stanford.braincat.rulepedia.events;

import java.nio.channels.Selector;

/**
 * Created by gcampagn on 4/30/15.
 */
public class TimeoutEventSource implements EventSource {
    private final long timeout;
    private final boolean repeat;
    private boolean active;
    private long endTime;

    public TimeoutEventSource(long timeout) {
        this(timeout, false);
    }

    public TimeoutEventSource(long timeout, boolean repeat) {
        if (timeout <= 0)
            throw new IllegalArgumentException("timeout must be positive");

        this.timeout = timeout;
        this.repeat = repeat;
    }

    private void start() {
        endTime = System.currentTimeMillis() + timeout;
        active = true;
    }

    @Override
    public void install(Selector sel) {
        start();
    }

    @Override
    public void uninstall() {
        active = false;
    }

    @Override
    public long getTimeout() {
        return System.currentTimeMillis() - endTime;
    }

    @Override
    public boolean checkEvent() {
        return active && System.currentTimeMillis() >= endTime;
    }

    @Override
    public void updateState() {
        if (checkEvent()) {
            if (repeat)
                start();
            else
                uninstall();
        }
    }
}
