package edu.stanford.braincat.rulepedia.channels;

import edu.stanford.braincat.rulepedia.events.TimeoutEventSource;

/**
 * Created by gcampagn on 4/30/15.
 * <p>
 * A trigger that updates every interval, but unlike SimpleEventTrigger
 * it does not necessary fire when the interval expires (that's up
 * to the subclass, which probably will do something in update() to
 * determine that)
 */
public abstract class PollingTrigger extends SingleEventTrigger<TimeoutEventSource> {
    protected PollingTrigger(long interval) {
        setSource(new TimeoutEventSource(interval));
    }
}
