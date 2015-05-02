package edu.stanford.braincat.rulepedia.model;

import java.util.Collection;

import edu.stanford.braincat.rulepedia.events.EventSource;
import edu.stanford.braincat.rulepedia.exceptions.RuleExecutionException;

/**
 * Created by gcampagn on 4/30/15.
 */
public interface Trigger {
    Collection<EventSource> getEventSources();

    void update() throws RuleExecutionException;

    boolean isFiring() throws RuleExecutionException;

    String toHumanString();

    boolean producesValue(String name, Class<? extends Value> type);

    Value getProducedValue(String name);
}
