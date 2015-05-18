package edu.stanford.braincat.rulepedia.channels.generic;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.ArrayMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.ScriptableObject;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import edu.stanford.braincat.rulepedia.channels.HTTPUtil;
import edu.stanford.braincat.rulepedia.events.EventSource;
import edu.stanford.braincat.rulepedia.events.TimeoutEventSource;
import edu.stanford.braincat.rulepedia.exceptions.RuleExecutionException;
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
        super(jsonObjectFactory.has("urlPrefix") ? jsonObjectFactory.getString("urlPrefix") : jsonObjectFactory.getString("objectId"));
        jsonFactory = jsonObjectFactory;
        id = jsonObjectFactory.getString("id");
        if (jsonObjectFactory.has("urlRegex"))
            pattern = Pattern.compile(jsonObjectFactory.getString("urlRegex"));
        else
            pattern = null;

        eventSourceMetas = new HashMap<>();
        JSONArray jsonEventSources = jsonFactory.getJSONArray("event-sources");
        for (int i = 0; i < jsonEventSources.length(); i++) {
            JSONObject jsonEventSource = jsonEventSources.getJSONObject(i);
            eventSourceMetas.put(jsonEventSource.getString("id"), jsonEventSource);
        }

        triggerMetas = new HashMap<>();
        JSONArray jsonTriggers = jsonFactory.getJSONArray("events");
        for (int i = 0; i < jsonTriggers.length(); i++) {
            JSONObject jsonTrigger = jsonTriggers.getJSONObject(i);
            triggerMetas.put(jsonTrigger.getString("id"), jsonTrigger);
        }

        actionMetas = new HashMap<>();
        JSONArray jsonActions = jsonFactory.getJSONArray("methods");
        for (int i = 0; i < jsonActions.length(); i++) {
            JSONObject jsonAction = jsonActions.getJSONObject(i);
            actionMetas.put(jsonAction.getString("id"), jsonAction);
        }
    }

    @Override
    public Channel create(String url) throws UnknownObjectException {
        if (pattern != null) {
            if (!pattern.matcher(url).matches())
                throw new UnknownObjectException(url);
        } else {
            if (!getPrefix().equals(url))
                throw new UnknownObjectException(url);
        }

        try {
            return new GenericChannel(this, url, jsonFactory.getString("description"));
        } catch (JSONException e) {
            throw new UnknownObjectException(url);
        }
    }

    @Override
    public Channel createPlaceholder(String url) {
        String text;

        try {
            text = jsonFactory.getString("text");
        } catch (JSONException e) {
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
        } catch (JSONException | TriggerValueTypeException e) {
            throw new UnknownChannelException(method);
        }
    }

    private void typeCheckParametersFor(JSONArray jsonParams,  Map<String, Value> params, Map<String, Class<? extends Value>> context)
            throws JSONException, TriggerValueTypeException {
        for (Map.Entry<String, Value> e : params.entrySet()) {
            e.getValue().typeCheck(context, findParamType(jsonParams, e.getKey()));
        }
    }

    public void typeCheckParameters(String method, Map<String, Value> params, Map<String, Class<? extends Value>> context) throws UnknownChannelException, TriggerValueTypeException {
        try {
            JSONObject jsonTrigger = triggerMetas.get(method);
            if (jsonTrigger != null) {
                typeCheckParametersFor(jsonTrigger.getJSONArray("params"), params, context);
                return;
            }

            JSONObject jsonAction = actionMetas.get(method);
            if (jsonAction != null) {
                typeCheckParametersFor(jsonAction.getJSONArray("params"), params, context);
                return;
            }

            throw new UnknownChannelException(method);
        } catch (JSONException e) {
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
        } catch (JSONException e) {
            throw new UnknownChannelException(method);
        }
    }

    private Map<String, EventSource> buildPrivateEventSources(JSONObject triggerMeta, Channel channel, Map<String, Value> params) throws
            JSONException, MalformedURLException, UnknownObjectException, TriggerValueTypeException {
        JSONArray eventSources = triggerMeta.getJSONArray("event-sources");
        Map<String, EventSource> result = new ArrayMap<>();

        for (int i = 0; i < eventSources.length(); i++) {
            JSONObject jsonSource = eventSources.getJSONObject(i);
            result.put(jsonSource.getString("id"), createEventSource(channel, jsonSource, params));
        }

        return result;
    }

    @Override
    public Trigger createTrigger(Channel channel, String method, Map<String, Value> params) throws UnknownObjectException, UnknownChannelException, TriggerValueTypeException {
        JSONObject triggerMeta = triggerMetas.get(method);

        if (triggerMeta == null)
            throw new UnknownChannelException(method);

        try {
            return new GenericTrigger(channel, triggerMeta.getString("id"), triggerMeta.getString("text"),
                    triggerMeta.getString("script"), buildPrivateEventSources(triggerMeta, channel, params), params);
        } catch (JSONException | MalformedURLException e) {

            throw new UnknownChannelException(method);
        }
    }

    @Override
    public Action createAction(Channel channel, String method, Map<String, Value> params) throws UnknownObjectException, UnknownChannelException, TriggerValueTypeException {
        JSONObject actionMeta = actionMetas.get(method);

        if (actionMeta == null)
            throw new UnknownChannelException(method);

        try {
            return new GenericAction(channel, actionMeta.getString("id"), actionMeta.getString("text"),
                    actionMeta.getString("script"), params);
        } catch (JSONException e) {
            throw new UnknownChannelException(method);
        }
    }

    public Collection<String> getEventSourceNames() {
        return eventSourceMetas.keySet();
    }

    private Number parseNumberOrParam(Object object, @Nullable Map<String, Value> params) throws JSONException, TriggerValueTypeException, UnknownObjectException {
        if (object instanceof Number)
            return (Number) object;
        else if (object instanceof String && params != null)
            return ((Value.Number) params.get(object).resolve(null)).getNumber();
        else
            throw new JSONException("invalid number value");
    }

    private EventSource createEventSource(Channel channel, JSONObject eventSourceMeta, @Nullable Map<String, Value> params) throws
            MalformedURLException, JSONException, TriggerValueTypeException, UnknownObjectException {
        switch (eventSourceMeta.getString("type")) {
            case "polling":
                return new TimeoutEventSource(parseNumberOrParam(eventSourceMeta.get("polling-interval"), params).longValue());
            case "polling-http":
                return new WebPollingEventSource(channel.getUrl(), parseNumberOrParam(eventSourceMeta.get("polling-interval"), params).longValue());
            default:
                throw new JSONException("invalid event source type");
        }
    }

    public EventSource createEventSource(Channel channel, String id)
            throws MalformedURLException, JSONException, TriggerValueTypeException, UnknownObjectException {
        JSONObject eventSourceMeta = eventSourceMetas.get(id);

        if (eventSourceMeta == null)
            throw new JSONException("no event source with id " + id);

        return createEventSource(channel, eventSourceMeta, null);
    }

    private RuleRunnable parseHTTPActionResult(ScriptableObject result) {
        final String method;
        if (ScriptableObject.hasProperty(result, "method"))
            method = ScriptableObject.getProperty(result, "method").toString().toLowerCase();
        else
            method = "get";

        final String url = ScriptableObject.getProperty(result, "url").toString();
        final String data;
        if (ScriptableObject.hasProperty(result, "data"))
            data = ScriptableObject.getProperty(result, "data").toString();
        else
            data = null;

        return new RuleRunnable() {
            @Override
            public void run(Context ctx) throws RuleExecutionException {
                try {
                    if (method.equals("post"))
                        HTTPUtil.postString(url, data);
                    else
                        HTTPUtil.getString(url);
                } catch(IOException e) {
                    throw new RuleExecutionException("Failed to execute HTTP action", e);
                }
            }
        };
    }

    private RuleRunnable parseIntentActionResult(ScriptableObject result) {
        final String action = (String) ScriptableObject.getProperty(result, "action");
        final String category;
        if (ScriptableObject.hasProperty(result, "category"))
            category = (String) ScriptableObject.getProperty(result, "category");
        else
            category = null;
        final String pkg;
        if (ScriptableObject.hasProperty(result, "package"))
            pkg = (String) ScriptableObject.getProperty(result, "package");
        else
            pkg = null;
        final boolean activity;
        if (ScriptableObject.hasProperty(result, "activity"))
            activity = (Boolean) ScriptableObject.getProperty(result, "activity");
        else
            activity = false;
        final Map<String, Serializable> extras = new ArrayMap<>();
        if (ScriptableObject.hasProperty(result, "extras"))
            JSUtil.parseExtras(extras, (ScriptableObject) ScriptableObject.getProperty(result, "extras"));

        return new RuleRunnable() {
            @Override
            public void run(Context ctx) throws RuleExecutionException {
                Intent intent = new Intent(action);
                if (category != null)
                    intent.addCategory(category);
                if (pkg != null)
                    intent.setPackage(pkg);
                for (Map.Entry<String, Serializable> e : extras.entrySet()) {
                    intent.putExtra(e.getKey(), e.getValue());
                }

                if (activity)
                    ctx.startActivity(intent);
                else
                    ctx.startService(intent);
            }
        };
    }

    public RuleRunnable parseActionResult(ScriptableObject result) throws RuleExecutionException {
        switch (ScriptableObject.getProperty(result, "type").toString()) {
            case "http":
                return parseHTTPActionResult(result);
            default:
                throw new RuleExecutionException("Action code returned invalid result");
        }
    }
}
