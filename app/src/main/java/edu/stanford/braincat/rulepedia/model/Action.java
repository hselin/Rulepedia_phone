package edu.stanford.braincat.rulepedia.model;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Map;

import edu.stanford.braincat.rulepedia.exceptions.RuleExecutionException;
import edu.stanford.braincat.rulepedia.exceptions.TriggerValueTypeException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;

/**
 * Created by gcampagn on 4/30/15.
 */
public interface Action {
    String OBJECT = "object";
    String METHOD = "method";
    String PARAMS = "params";

    Channel getChannel();

    Collection<ObjectPool.Object> getPlaceholders();

    void resolve() throws UnknownObjectException;

    void typeCheck(Map<String, Class<? extends Value>> context) throws TriggerValueTypeException;

    void execute(Context ctx, Map<String, Value> context) throws UnknownObjectException, RuleExecutionException;

    String toHumanString();

    JSONObject toJSON() throws JSONException;
}
