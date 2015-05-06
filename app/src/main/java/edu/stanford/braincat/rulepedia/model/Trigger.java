package edu.stanford.braincat.rulepedia.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;

import edu.stanford.braincat.rulepedia.events.EventSource;
import edu.stanford.braincat.rulepedia.exceptions.RuleExecutionException;

/**
 * Created by gcampagn on 4/30/15.
 */
public interface Trigger {
    String OBJECT = "object";
    String TRIGGER = "trigger";
    String PARAMS = "params";

    Collection<EventSource> getEventSources();

    void update() throws RuleExecutionException;

    boolean isFiring() throws RuleExecutionException;

    String toHumanString();

    JSONObject toJSON() throws JSONException;

    boolean producesValue(String name, Class<? extends Value> type);

    Value getProducedValue(String name) throws RuleExecutionException;
}
