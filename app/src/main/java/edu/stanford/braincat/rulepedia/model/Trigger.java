package edu.stanford.braincat.rulepedia.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Map;

import edu.stanford.braincat.rulepedia.events.EventSource;
import edu.stanford.braincat.rulepedia.exceptions.RuleExecutionException;
import edu.stanford.braincat.rulepedia.exceptions.TriggerValueTypeException;

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

    void typeCheck(Map<String, Class<? extends Value>> context) throws TriggerValueTypeException;

    void updateContext(Map<String, Value> context) throws RuleExecutionException;
}
