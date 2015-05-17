package edu.stanford.braincat.rulepedia.channels.generic;

import android.util.ArrayMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import edu.stanford.braincat.rulepedia.channels.Util;
import edu.stanford.braincat.rulepedia.events.EventSource;
import edu.stanford.braincat.rulepedia.exceptions.RuleExecutionException;
import edu.stanford.braincat.rulepedia.exceptions.TriggerValueTypeException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownChannelException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;
import edu.stanford.braincat.rulepedia.model.Channel;
import edu.stanford.braincat.rulepedia.model.ObjectPool;
import edu.stanford.braincat.rulepedia.model.Trigger;
import edu.stanford.braincat.rulepedia.model.Value;

/**
 * Created by gcampagn on 5/15/15.
 */
public class GenericTrigger implements Trigger {
    private final String id;
    private final String text;
    private final Map<String, EventSource> eventSources;
    private Channel channel;
    private final String scriptBody;
    private Function script;
    private Scriptable thisArg;
    private final Map<String, Value> parameters;
    private NativeObject cachedJSParameters;
    private NativeObject eventSourceValues;
    private Map<String, Value> produced;

    public GenericTrigger(Channel channel, String id, String text, String scriptBody, Map<String, EventSource> eventSources, Map<String, Value> params)
            throws TriggerValueTypeException, UnknownObjectException {
        super();
        this.id = id;
        this.text = text;
        this.channel = channel;
        this.scriptBody = scriptBody;
        this.eventSources = eventSources;

        parameters = new ArrayMap<>();
        for (Map.Entry<String, Value> e : params.entrySet()) {
            parameters.put(e.getKey(), e.getValue().resolve(null));
        }
    }

    @Override
    public Channel getChannel() {
        return channel;
    }

    @Override
    public Collection<EventSource> getEventSources() {
        return eventSources.values();
    }

    @Override
    public Collection<ObjectPool.Object> getPlaceholders() {
        Collection<ObjectPool.Object> result = new HashSet<>();

        Channel currentChannel = channel;
        if (currentChannel.isPlaceholder())
            result.add(currentChannel);
        for (Value value : parameters.values()) {
            try {
                if (value instanceof Value.Contact) {
                    try {
                        Value.DirectObject resolved = (Value.DirectObject) value.resolve(null);
                        if (resolved.getObject().isPlaceholder())
                            result.add(resolved.getObject());
                    } catch (UnknownObjectException e) {
                        // nothing to do
                    }
                } else if (value instanceof Value.DirectObject) {
                    if (((Value.DirectObject) value).getObject().isPlaceholder())
                        result.add(((Value.DirectObject) value).getObject());
                }
            } catch (TriggerValueTypeException e) {
                // nothing to do
            }
        }

        return result;
    }

    @Override
    public void update() throws RuleExecutionException {
        try {
            NativeObject newEventSourceValues = new NativeObject();

            for (Map.Entry<String, EventSource> e : eventSources.entrySet()) {
                EventSource source = e.getValue();

                if (source.checkEvent()) {
                    if (source instanceof WebPollingEventSource)
                        ScriptableObject.putProperty(newEventSourceValues, e.getKey(), Util.readString(((WebPollingEventSource) source).getLastConnection()));
                    else
                        ScriptableObject.putProperty(newEventSourceValues, e.getKey(), source.checkEvent());
                }
            }

            eventSourceValues = newEventSourceValues;
        } catch (IOException e) {
            throw new RuleExecutionException("IO exception while reading from event source", e);
        }
    }

    @Override
    public boolean isFiring() throws RuleExecutionException {
        if (cachedJSParameters == null)
            cachedJSParameters = JSUtil.parametersToJavascript(parameters);
        try {
            NativeObject jsProducedCtx = new NativeObject();
            Boolean result = (Boolean) ((GenericChannel) channel).callFunction(script, thisArg,
                    cachedJSParameters, eventSourceValues, jsProducedCtx);
            produced = JSUtil.javascriptToParameters(jsProducedCtx);
            return result;
        } catch (Exception e) {
            throw new RuleExecutionException("Exception while evaluating trigger script", e);
        }
    }

    @Override
    public String toHumanString() {
        return text;
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(Trigger.OBJECT, channel.getUrl());
        json.put(Trigger.TRIGGER, id);

        JSONArray jsonParams = new JSONArray();
        for (Map.Entry<String, Value> e : parameters.entrySet()) {
            jsonParams.put(e.getValue().toJSON(e.getKey()));
        }
        json.put(Trigger.PARAMS, jsonParams);
        return json;
    }

    @Override
    public void resolve() throws UnknownObjectException {
        Channel newChannel = channel.resolve();
        if (!(newChannel instanceof GenericChannel))
            throw new UnknownObjectException(newChannel.getUrl());

        try {
            eventSources.putAll(((GenericChannel) channel).getEventSources());
        } catch (MalformedURLException | JSONException | TriggerValueTypeException e) {
            throw new UnknownObjectException(newChannel.getUrl());
        }

        script = ((GenericChannel) newChannel).compileFunction(scriptBody);
        thisArg = new NativeObject();

        channel = newChannel;
    }

    @Override
    public void typeCheck(Map<String, Class<? extends Value>> context) {
        try {
            ((GenericChannelFactory) getChannel().getFactory()).updateGeneratesType(id, context);
        } catch (UnknownChannelException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public void updateContext(Map<String, Value> context) throws RuleExecutionException {
        context.putAll(produced);
    }
}
