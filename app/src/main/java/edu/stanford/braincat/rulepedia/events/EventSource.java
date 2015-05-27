package edu.stanford.braincat.rulepedia.events;

import android.content.Context;

import java.io.IOException;

/**
 * Created by gcampagn on 4/30/15.
 */
public interface EventSource {
    /**
     * Start watching for events on this source.
     */
    void install(Context ctx, EventSourceHandler handler) throws IOException;

    /**
     * Stop watching for events on this source.
     */
    void uninstall(Context ctx) throws IOException;

    /**
     * Returns true if there is an event pending, false otherwise
     */
    boolean checkEvent() throws IOException;

    /**
     * Update state according to the received event
     */
    void updateState() throws IOException;
}
