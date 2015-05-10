package edu.stanford.braincat.rulepedia.channels.web;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

import java.io.IOException;
import java.util.Map;

import edu.stanford.braincat.rulepedia.channels.ScriptableObject;
import edu.stanford.braincat.rulepedia.channels.SingleEventTrigger;
import edu.stanford.braincat.rulepedia.channels.omdb.OMDBObjectFactory;
import edu.stanford.braincat.rulepedia.events.TimeoutEventSource;
import edu.stanford.braincat.rulepedia.exceptions.RuleExecutionException;
import edu.stanford.braincat.rulepedia.exceptions.TriggerValueTypeException;
import edu.stanford.braincat.rulepedia.model.Trigger;
import edu.stanford.braincat.rulepedia.model.Value;

/**
 * Created by gcampagn on 5/8/15.
 */
public class WebRefreshingPollingTrigger extends SingleEventTrigger<TimeoutEventSource> {
    private final String id;
    private final String text;
    private final WebObject object;
    private final Function script;
    private Scriptable result;

    public WebRefreshingPollingTrigger(WebObject object, String id, String text, TimeoutEventSource eventSource, String script) {
        super(eventSource);
        this.id = id;
        this.text = text;
        this.object = object;
        this.script = object.compileFunction(script);
    }

    @Override
    public void update() throws RuleExecutionException {
        try {
            object.refresh();
        } catch(IOException ioe) {
            throw new RuleExecutionException("IO exception while refreshing object", ioe);
        }
    }

    @Override
    public boolean isFiring() throws RuleExecutionException {
        Object result = object.callFunction(script, object.getData());
        return result instanceof ScriptableObject;
    }

    @Override
    public String toHumanString() {
        return text;
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(Trigger.OBJECT, object.getUrl());
        json.put(Trigger.TRIGGER, OMDBObjectFactory.MOVIE_RELEASED);
        json.put(Trigger.PARAMS, new JSONArray());
        return json;
    }

    @Override
    public void typeCheck(Map<String, Class<? extends Value>> context) throws TriggerValueTypeException {
        // FIXME
    }

    @Override
    public void updateContext(Map<String, Value> context) throws RuleExecutionException {
        // FIXME
    }
}
