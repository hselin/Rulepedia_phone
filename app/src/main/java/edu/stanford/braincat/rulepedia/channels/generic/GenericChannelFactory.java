package edu.stanford.braincat.rulepedia.channels.generic;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import edu.stanford.braincat.rulepedia.events.EventSource;
import edu.stanford.braincat.rulepedia.events.TimeoutEventSource;
import edu.stanford.braincat.rulepedia.exceptions.TriggerValueTypeException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownChannelException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;
import edu.stanford.braincat.rulepedia.model.Action;
import edu.stanford.braincat.rulepedia.model.Channel;
import edu.stanford.braincat.rulepedia.model.ChannelFactory;
import edu.stanford.braincat.rulepedia.model.PlaceholderChannel;
import edu.stanford.braincat.rulepedia.model.Trigger;
import edu.stanford.braincat.rulepedia.model.Value;

/**
 * Created by gcampagn on 5/8/15.
 */
public class GenericChannelFactory extends ChannelFactory {
    private final JSONObject jsonFactory;
    private final String id;
    private final Pattern pattern;

    private final Map<String, JSONObject> eventSourceMetas;
    private final Map<String, JSONObject> triggerMetas;
    private final Map<String, JSONObject> actionMetas;

    public GenericChannelFactory(JSONObject jsonObjectFactory) throws JSONException {
        super(jsonObjectFactory.getString("urlPrefix"));
        jsonFactory = jsonObjectFactory;
        id = jsonObjectFactory.getString("id");
        pattern = Pattern.compile(jsonObjectFactory.getString("urlRegex"));

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
        JSONArray jsonActions = jsonFactory.getJSONArray("methods");
        for (int i = 0; i < jsonActions.length(); i++) {
            JSONObject jsonAction = jsonEventSources.getJSONObject(i);
            actionMetas.put(jsonAction.getString("id"), jsonAction);
        }
    }

    @Override
    public Channel create(String url) throws UnknownObjectException {
        if (!pattern.matcher(url).matches())
            throw new UnknownObjectException(url);

        try {
            return new GenericChannel(this, url, jsonFactory.getString("id"), jsonFactory.getString("text"));
        } catch(JSONException e) {
            throw new UnknownObjectException(url);
        }
    }

    @Override
    public Channel createPlaceholder(String url) {
        String text;

        try {
            text = jsonFactory.getString("text");
        } catch(JSONException e) {
            text = "a generic channel";
        }

        return new PlaceholderChannel(this, url, text);
    }

    @Override
    public String getName() {
        return id;
    }

    private static Class<? extends Value> classForTypeName(String paramtype) throws TriggerValueTypeException {
        switch (paramtype) {
            case "text":
            case "textarea":
                return Value.Text.class;
            case "time":
            case "number":
                return Value.Number.class;
            case "select":
                return Value.Select.class;
            case "picture":
                return Value.Picture.class;
            case "contact":
            case "message-destination":
                return Value.Contact.class;
            default:
                throw new TriggerValueTypeException("invalid type " + paramtype);
        }
    }

    private void updateGeneratesTypeFor(JSONArray generates, Map<String, Class<? extends Value>> context) throws JSONException, TriggerValueTypeException {
        for (int i = 0; i < generates.length(); i++) {
            JSONObject generatespec = generates.getJSONObject(i);
            String paramtype = generatespec.getString("type");
            context.put(generatespec.getString("id"), classForTypeName(paramtype));
        }
    }

    public void updateGeneratesType(String method, Map<String, Class<? extends Value>> context) throws UnknownChannelException {
        try {
            JSONObject jsonTrigger = triggerMetas.get(method);
            if (jsonTrigger != null) {
                updateGeneratesTypeFor(jsonTrigger.getJSONArray("params"), context);
                return;
            }

            JSONObject jsonAction = actionMetas.get(method);
            if (jsonAction != null) {
                updateGeneratesTypeFor(jsonAction.getJSONArray("params"), context);
                return;
            }

            throw new UnknownChannelException(method);
        } catch(JSONException|TriggerValueTypeException e) {
            throw new UnknownChannelException(method);
        }
    }

    private static Class<? extends Value> findParamType(JSONArray jsonParams, String name) throws JSONException, TriggerValueTypeException {
        for (int i = 0; i < jsonParams.length(); i++) {
            JSONObject paramspec = jsonParams.getJSONObject(i);
            if (!paramspec.get("id").equals(name))
                continue;
            return classForTypeName(paramspec.getString("type"));
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
    public Trigger createTrigger(Channel channel, String method, Map<String, Value> params) throws UnknownObjectException, UnknownChannelException, TriggerValueTypeException {
        JSONObject triggerMeta = triggerMetas.get(method);

        if (triggerMeta == null)
            throw new UnknownChannelException(method);

        try {
            return new GenericTrigger(channel, triggerMeta.getString("id"), triggerMeta.getString("text"),
                    triggerMeta.getString("script"), params);
        } catch(JSONException e) {
            throw new UnknownChannelException(method);
        }
    }

    @Override
    public Action createAction(Channel channel, String method, Map<String, Value> params) throws UnknownObjectException, UnknownChannelException, TriggerValueTypeException {
        throw new UnknownChannelException(method);
    }

    public Collection<String> getEventSourceNames() {
        return eventSourceMetas.keySet();
    }

    public EventSource createEventSource(GenericChannel channel, String id) throws MalformedURLException, JSONException {
        JSONObject eventSourceMeta = eventSourceMetas.get(id);

        if (eventSourceMeta == null)
            throw new JSONException("no event source with id " + id);

        switch (eventSourceMeta.getString("type")) {
            case "polling":
                return new TimeoutEventSource(eventSourceMeta.getLong("polling-interval"));
            case "polling-http":
                return new WebPollingEventSource(channel.getUrl(), eventSourceMeta.getLong("polling-interval"));
            default:
                throw new JSONException("invalid event source type");
        }
    }
}
