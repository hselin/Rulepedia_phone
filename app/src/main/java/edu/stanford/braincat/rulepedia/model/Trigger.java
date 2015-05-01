package edu.stanford.braincat.rulepedia.model;

import java.io.IOException;
import java.util.Collection;

import edu.stanford.braincat.rulepedia.events.EventSource;

/**
 * Created by gcampagn on 4/30/15.
 */
public interface Trigger {
    Collection<EventSource> getEventSources();

    void update() throws IOException;

    boolean isFiring();

    String toHumanString();
}
