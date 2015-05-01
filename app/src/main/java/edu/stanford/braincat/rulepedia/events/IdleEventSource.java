package edu.stanford.braincat.rulepedia.events;

import java.io.IOException;
import java.nio.channels.Selector;

/**
 * Created by gcampagn on 4/30/15.
 *
 * An event source that has an event whenever the active property is true,
 * at any mainloop iteration. Used for scheduling internal events
 */
public class IdleEventSource implements EventSource {
    private boolean active;

    public IdleEventSource() {}

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean _active) {
        active = _active;
    }

    @Override
    public void install(Selector sel) throws IOException {
        // nothing to do
    }

    @Override
    public void uninstall() throws IOException {
        // nothing to do
    }

    @Override
    public long getTimeout() {
        return 0;
    }

    @Override
    public boolean checkEvent() {
        return active;
    }

    @Override
    public void updateState() {
        // nothing to do
    }
}
