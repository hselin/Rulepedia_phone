package edu.stanford.braincat.rulepedia.channels.generic;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;

import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import edu.stanford.braincat.rulepedia.channels.omdb.OMDBChannelFactory;
import edu.stanford.braincat.rulepedia.events.EventSource;
import edu.stanford.braincat.rulepedia.exceptions.RuleExecutionException;
import edu.stanford.braincat.rulepedia.exceptions.TriggerValueTypeException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;
import edu.stanford.braincat.rulepedia.model.Channel;
import edu.stanford.braincat.rulepedia.model.ObjectPool;
import edu.stanford.braincat.rulepedia.model.Rule;
import edu.stanford.braincat.rulepedia.model.Trigger;
import edu.stanford.braincat.rulepedia.model.Value;

/**
 * Created by gcampagn on 5/15/15.
 */
public class GenericTrigger implements Trigger {
    private final String id;
    private final String text;
    private Channel channel;
    private final String scriptBody;
    private Function script;
    private Map<String, Value> parameters;
    private NativeObject eventSourceValues;

    public GenericTrigger(Channel channel, String id, String text, String scriptBody, HashMap<String, Value> params)
            throws TriggerValueTypeException, UnknownObjectException {
        super();
        this.id = id;
        this.text = text;
        this.channel = channel;
        this.scriptBody = scriptBody;

        parameters = new HashMap<>();
        for (Map.Entry<String, Value> e : params.entrySet()) {
            parameters.put(e.getKey(), e.getValue().resolve(null));
        }
    }

    public Channel getChannel() {
        return channel;
    }

    public Collection<ObjectPool.Object> getPlaceholders() {
        Collection<ObjectPool.Object> result = new HashSet<>();

        Channel currentChannel = channel;
        if (currentChannel.isPlaceholder())
            result.add(currentChannel);

        return result;
    }

    @Override
    public void update() throws RuleExecutionException {
        try {
            NativeObject newEventSourceValues = new NativeObject();

            for (Map.Entry<String, EventSource> e : ((GenericChannel) channel).getEventSources().entrySet()) {
                EventSource source = e.getValue();
                if (source instanceof WebPollingEventSource)
                    newEventSourceValues.gi
            }
        } catch(MalformedURLException|JSONException e) {
            throw new RuleExecutionException("Failed to obtain event sources", e);
        }
    }

    @Override
    public boolean isFiring() throws RuleExecutionException {
        Object result = ((GenericChannel)channel).callFunction(script, ((GenericChannel)channel).getData());
        return result instanceof ScriptableChannel;
    }

    @Override
    public String toHumanString() {
        return text;
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(Trigger.OBJECT, channel.getUrl());
        json.put(Trigger.TRIGGER, OMDBChannelFactory.MOVIE_RELEASED);
        json.put(Trigger.PARAMS, new JSONArray());
        return json;
    }

    @Override
    public void resolve() throws UnknownObjectException {
        Channel newChannel = channel.resolve();
        if (!(newChannel instanceof GenericChannel))
            throw new UnknownObjectException(newChannel.getUrl());

        script = ((GenericChannel) newChannel).compileFunction(scriptBody);
        try {
            setSource(((GenericChannel) newChannel).getEventSource(eventSourceName));
        } catch(JSONException e) {
            throw new UnknownObjectException(newChannel.getUrl());
        }
        channel = newChannel;
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
