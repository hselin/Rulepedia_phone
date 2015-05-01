package edu.stanford.braincat.rulepedia.channels;

import java.util.Arrays;
import java.util.Collection;

import edu.stanford.braincat.rulepedia.events.EventSource;
import edu.stanford.braincat.rulepedia.model.Trigger;

/**
 * Created by gcampagn on 4/30/15.
 *
 * Helper code for triggers that have only one associated event source.
 */
public abstract class SingleEventTrigger implements Trigger {
    private final EventSource source;

    protected SingleEventTrigger(EventSource source) {
        this.source = source;
    }

    protected EventSource getSource() {
        return source;
    }

    @Override
    public Collection<EventSource> getEventSources() {
        return Arrays.asList(new EventSource[] { source });
    }
}
