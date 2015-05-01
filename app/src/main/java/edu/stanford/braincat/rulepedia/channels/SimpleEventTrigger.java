package edu.stanford.braincat.rulepedia.channels;

import edu.stanford.braincat.rulepedia.events.EventSource;

/**
 * Created by gcampagn on 4/30/15.
 *
 * A trigger that is firing if and only if the associated event source
 * reports something
 *
 * The trigger will keep firing or not according to the behavior
 * of the event source (edge triggered vs level trigger)
 */
public abstract class SimpleEventTrigger<K extends EventSource> extends SingleEventTrigger<K> {
    protected SimpleEventTrigger(K source) {
        super(source);
    }

    public boolean isFiring() {
        return getSource().checkEvent();
    }
}
