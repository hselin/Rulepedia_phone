package edu.stanford.braincat.rulepedia.channels.web;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import edu.stanford.braincat.rulepedia.channels.HTTPHelper;
import edu.stanford.braincat.rulepedia.channels.RefreshableObject;
import edu.stanford.braincat.rulepedia.channels.ScriptableObject;
import edu.stanford.braincat.rulepedia.exceptions.RuleExecutionException;
import edu.stanford.braincat.rulepedia.exceptions.TriggerValueTypeException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownChannelException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;
import edu.stanford.braincat.rulepedia.model.Action;
import edu.stanford.braincat.rulepedia.model.Trigger;
import edu.stanford.braincat.rulepedia.model.Value;

/**
 * Created by gcampagn on 5/8/15.
 */
public class WebObject extends ScriptableObject implements RefreshableObject {
    private final String text;
    private final String id;
    private final Map<String, JSONObject> eventSourceMetas;
    private final Map<String, JSONObject> triggerMetas;
    private final Map<String, JSONObject> actionMetas;

    private String response;

    public WebObject(String url, JSONObject jsonFactory) throws JSONException {
        super(url);
        id = jsonFactory.getString("id");
        text = jsonFactory.getString("text");

        eventSourceMetas = new HashMap<>();
        JSONArray jsonEventSources = jsonFactory.getJSONArray("event-sources");
        for (int i = 0; i < jsonEventSources.length(); i++) {
            JSONObject jsonEventSource = jsonEventSources.getJSONObject(i);
            eventSourceMetas.put(jsonEventSource.getString("id"), jsonEventSource);
        }

        triggerMetas = new HashMap<>();
        JSONArray jsonTriggers = jsonFactory.getJSONArray("events");
        for (int i = 0; i < jsonTriggers.length(); i++) {
            JSONObject jsonTrigger = jsonEventSources.getJSONObject(i);
            triggerMetas.put(jsonTrigger.getString("id"), jsonTrigger);
        }

        actionMetas = new HashMap<>();
        JSONArray jsonActions = jsonFactory.getJSONArray("event-sources");
        for (int i = 0; i < jsonActions.length(); i++) {
            JSONObject jsonAction = jsonEventSources.getJSONObject(i);
            actionMetas.put(jsonAction.getString("id"), jsonAction);
        }

        // FIXME auth
    }

    @Override
    public synchronized void refresh() throws IOException {
        response = HTTPHelper.getString(getUrl());
    }

    private void checkData() throws RuleExecutionException {
        if (response == null)
            throw new RuleExecutionException("Movie data not available");
    }

    public synchronized String getData() throws RuleExecutionException {
        checkData();
        return response;
    }

    @Override
    public String toHumanString() {
        return text;
    }

    @Override
    public String getType() {
        return id;
    }

    private static Class<? extends Value> findParamType(JSONArray jsonParams, String name) throws JSONException, TriggerValueTypeException {
        for (int i = 0; i < jsonParams.length(); i++) {
            JSONObject paramspec = jsonParams.getJSONObject(i);
            if (!paramspec.get("id").equals(name))
                continue;
            String paramtype = paramspec.getString("type");
            switch(paramtype) {
                case "text":
                case "textarea":
                    return Value.Text.class;
                case "contact":
                case "message-destination":
                    return Value.Object.class;
                default:
                    throw new TriggerValueTypeException("invalid type " + paramtype);
            }
        }

        throw new TriggerValueTypeException("invalid param " + name);
    }

    @Override
    public Class<? extends Value> getParamType(String method, String name) throws UnknownChannelException, TriggerValueTypeException {
        try {
            JSONObject jsonTrigger = triggerMetas.get(method);
            if (jsonTrigger != null)
                return findParamType(jsonTrigger.getJSONArray("params"), name);

            JSONObject jsonAction = actionMetas.get(method);
            if (jsonAction != null)
                return findParamType(jsonAction.getJSONArray("params"), name);

            throw new UnknownChannelException(method);
        } catch(JSONException e) {
            throw new UnknownChannelException(method);
        }
    }

    @Override
    public Trigger createTrigger(String method, Map<String, Value> params) throws UnknownObjectException, UnknownChannelException, TriggerValueTypeException {
        return null;
    }

    @Override
    public Action createAction(String method, Map<String, Value> params) throws UnknownObjectException, UnknownChannelException, TriggerValueTypeException {
        return null;
    }
}
