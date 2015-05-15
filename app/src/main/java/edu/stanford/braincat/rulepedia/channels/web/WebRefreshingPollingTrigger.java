package edu.stanford.braincat.rulepedia.channels.web;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import edu.stanford.braincat.rulepedia.channels.ScriptableChannel;
import edu.stanford.braincat.rulepedia.channels.SingleEventTrigger;
import edu.stanford.braincat.rulepedia.channels.omdb.OMDBChannelFactory;
import edu.stanford.braincat.rulepedia.events.EventSource;
import edu.stanford.braincat.rulepedia.exceptions.RuleExecutionException;
import edu.stanford.braincat.rulepedia.exceptions.TriggerValueTypeException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;
import edu.stanford.braincat.rulepedia.model.Channel;
import edu.stanford.braincat.rulepedia.model.ObjectPool;
import edu.stanford.braincat.rulepedia.model.Trigger;
import edu.stanford.braincat.rulepedia.model.Value;

/**
 * Created by gcampagn on 5/8/15.
 */
public class WebRefreshingPollingTrigger extends SingleEventTrigger<EventSource> {
    private final String id;
    private final String text;
    private Channel channel;
    private final String eventSourceName;
    private final String scriptBody;
    private Function script;
    private Scriptable result;

    public WebRefreshingPollingTrigger(Channel channel, String id, String text, String eventSourceName, String scriptBody) {
        super();
        this.id = id;
        this.text = text;
        this.channel = channel;
        this.eventSourceName = eventSourceName;
        this.scriptBody = scriptBody;
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
            ((WebChannel)channel).refresh();
        } catch(IOException ioe) {
            throw new RuleExecutionException("IO exception while refreshing object", ioe);
        }
    }

    @Override
    public boolean isFiring() throws RuleExecutionException {
        Object result = ((WebChannel)channel).callFunction(script, ((WebChannel)channel).getData());
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
        if (!(newChannel instanceof WebChannel))
            throw new UnknownObjectException(newChannel.getUrl());

        script = ((WebChannel) newChannel).compileFunction(scriptBody);
        try {
            setSource(((WebChannel) newChannel).getEventSource(eventSourceName));
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
