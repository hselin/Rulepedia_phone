package edu.stanford.braincat.rulepedia.events;

import java.io.IOException;
import java.nio.channels.Selector;

/**
 * Created by gcampagn on 4/30/15.
 */
public interface EventSource {
    /**
     * Start watching for events on this source.
     */
    void install(Selector sel) throws IOException;

    /**
     * Stop watching for events on this source.
     */
    void uninstall() throws IOException;

    /**
     * Returns an optional timeout for blocking on this source,
     * in milliseconds
     * 0 means immediate return, Long.MAX_VALUE means no timeout
     */
    long getTimeout();

    /**
     * Returns true if there is an event pending, false otherwise
     */
    boolean checkEvent();

    /**
     * Update state according to the received event
     */
    void updateState();
}
