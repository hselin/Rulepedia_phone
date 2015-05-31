package edu.stanford.braincat.rulepedia.channels.generic;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.ArrayMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.ScriptableObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import edu.stanford.braincat.rulepedia.channels.HTTPUtil;
import edu.stanford.braincat.rulepedia.channels.ServiceBinder;
import edu.stanford.braincat.rulepedia.channels.email.EmailSender;
import edu.stanford.braincat.rulepedia.channels.omlet.OmletMessageEventSource;
import edu.stanford.braincat.rulepedia.events.EventSource;
import edu.stanford.braincat.rulepedia.events.IntentEventSource;
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
import mobisocial.osm.IOsmService;

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
        super(jsonObjectFactory.has("urlPrefix") && jsonObjectFactory.getString("urlPrefix").length() > 0 ?
                jsonObjectFactory.getString("urlPrefix") : jsonObjectFactory.getString("objectId"));
        jsonFactory = jsonObjectFactory;
        id = jsonObjectFactory.getString("id");
        if (jsonObjectFactory.has("urlRegex") && jsonObjectFactory.getString("urlRegex").length() > 0)
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
            return new GenericChannel(this, url, id, jsonFactory.getString("description"),
                    jsonFactory.has("services") ? jsonFactory.getJSONArray("services") : new JSONArray());
        } catch (JSONException|UnknownChannelException e) {
            throw new UnknownObjectException(url, e);
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
            case Value.Text.ID:
            case "textarea":
                return Value.Text.class;
            case "time":
            case Value.Number.ID:
                return Value.Number.class;
            case Value.Select.ID:
                return Value.Select.class;
            case Value.Picture.ID:
                return Value.Picture.class;
            case Value.Contact.ID:
            case "message-destination":
                return Value.Contact.class;
            case Value.Device.ID:
                return Value.Device.class;
            default:
                throw new TriggerValueTypeException("invalid type " + paramtype);
        }
    }

    private static void updateGeneratesTypeFor(JSONArray generates, Map<String, Class<? extends Value>> context) throws JSONException, TriggerValueTypeException {
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
            throw new UnknownChannelException(method, e);
        }
    }

    private static void typeCheckParametersFor(JSONArray jsonParams,  Map<String, Value> params, Map<String, Class<? extends Value>> context)
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
            throw new UnknownChannelException(method, e);
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
            throw new UnknownChannelException(method, e);
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
            throw new UnknownChannelException(method, e);
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
            throw new UnknownChannelException(method, e);
        }
    }

    public Collection<String> getEventSourceNames() {
        return eventSourceMetas.keySet();
    }

    private static Number parseNumber(Object object, @Nullable Map<String, Value> params) throws JSONException, TriggerValueTypeException, UnknownObjectException {
        if (object instanceof Number)
            return (Number) object;
        else if (object instanceof String && params != null) {
            if (((String)object).startsWith("{{"))
                return ((Value.Number) params.get(((String) object).substring(2, ((String) object).length()-2)).resolve(null)).getNumber();
            else
                return ((Value.Number) params.get(object).resolve(null)).getNumber();
        } else
            throw new JSONException("invalid number value");
    }

    private static String parseText(String str, String url, @Nullable Map<String, Value> params) throws TriggerValueTypeException, UnknownObjectException {
        String replace = "{{url}}";
        if (str.contains(replace))
            str = str.replace(replace, url);

        if (params == null)
            return str;

        for (Map.Entry<String, Value> e : params.entrySet()) {
            replace = "{{" + e.getKey() + "}}";
            if (str.contains(replace))
                str = str.replace(replace, e.getValue().resolve(null).toString());
        }

        return str;
    }

    private static EventSource createEventSource(Channel channel, JSONObject eventSourceMeta, @Nullable Map<String, Value> params) throws
            MalformedURLException, JSONException, TriggerValueTypeException, UnknownObjectException {
        switch (eventSourceMeta.getString("type")) {
            case "polling":
                return new TimeoutEventSource(parseNumber(eventSourceMeta.get("polling-interval"), params).longValue());
            case "polling-http": {
                String url;
                if (eventSourceMeta.has("url"))
                    url = parseText(eventSourceMeta.getString("url"), channel.getUrl(), params);
                else
                    url = channel.getUrl();
                return new WebPollingEventSource(url, parseNumber(eventSourceMeta.get("polling-interval"), params).longValue());
            }
            case "broadcast-receiver":
                IntentFilter filter = new IntentFilter(parseText(eventSourceMeta.getString("intent-action"), channel.getUrl(), params));
                if (eventSourceMeta.has("intent-category"))
                    filter.addCategory(parseText(eventSourceMeta.getString("intent-category"), channel.getUrl(), params));
                return new IntentEventSource(filter);
            case "sse":
                throw new UnsupportedOperationException("Server Sent Events are not yet implemented");
            case "omlet":
                return new OmletMessageEventSource();
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

    private static RuleRunnable parseHTTPActionResult(ScriptableObject result) {
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
            public void run(Context ctx, GenericChannel channel) throws RuleExecutionException {
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

    private static RuleRunnable parseIntentActionResult(ScriptableObject result) {
        final Intent intent = JSUtil.javascriptToIntent(result);
        final boolean activity;
        if (ScriptableObject.hasProperty(result, "activity"))
            activity = (Boolean) ScriptableObject.getProperty(result, "activity");
        else
            activity = false;

        return new RuleRunnable() {
            @Override
            public void run(Context ctx, GenericChannel channel) throws RuleExecutionException {
                if (activity)
                    ctx.startActivity(intent);
                else
                    ctx.startService(intent);
            }
        };
    }

    private static RuleRunnable parseOmletActionResult(ScriptableObject result) {
        final String groupUri = ScriptableObject.getProperty(result, "groupUri").toString();
        final String messageType = ScriptableObject.getProperty(result, "messageType").toString();
        final Object jsonObject = ScriptableObject.getProperty(result, "message");

        return new RuleRunnable() {
            @Override
            public void run(Context ctx, GenericChannel channel) throws RuleExecutionException {
                IOsmService omletService = (IOsmService) channel.getService("omlet");

                if (omletService == null)
                    throw new RuleExecutionException("Omlet service is not available");

                String json = channel.toJSON(jsonObject);
                try {
                    omletService.sendObj(Uri.parse(groupUri), messageType, json);
                } catch(RemoteException e) {
                    throw new RuleExecutionException(e);
                }
            }
        };
    }

    private static RuleRunnable parseEmailActionResult(ScriptableObject result) {
        final String to = ScriptableObject.getProperty(result, "to").toString();
        final String subject = ScriptableObject.getProperty(result, "subject").toString();
        final String body = ScriptableObject.getProperty(result, "body").toString();

        return new RuleRunnable() {
            @Override
            public void run(Context ctx, GenericChannel channel) throws RuleExecutionException {
                try {
                    EmailSender.sendEmail(to, subject, body);
                } catch(IOException e) {
                    throw new RuleExecutionException(e);
                }
            }
        };
    }

    public static RuleRunnable parseActionResult(ScriptableObject result) throws RuleExecutionException {
        switch (ScriptableObject.getProperty(result, "type").toString()) {
            case "http":
                return parseHTTPActionResult(result);
            case "intent":
                return parseIntentActionResult(result);
            case "omlet":
                return parseOmletActionResult(result);
            case "email":
                return parseEmailActionResult(result);
            default:
                throw new RuleExecutionException("Action code returned invalid result");
        }
    }

    public static ServiceBinder createServiceBinder(String type) throws UnknownChannelException {
        switch (type) {
            case "omlet": {
                Intent intent = new Intent("mobisocial.intent.action.BIND_SERVICE");
                intent.setPackage("mobisocial.omlet");
                return new ServiceBinder(intent);
            }

            default:
                throw new UnknownChannelException(type);
        }
    }
}
