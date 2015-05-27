package edu.stanford.braincat.rulepedia.channels.generic;

import android.content.Context;
import android.util.ArrayMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import edu.stanford.braincat.rulepedia.events.EventSource;
import edu.stanford.braincat.rulepedia.exceptions.RuleExecutionException;
import edu.stanford.braincat.rulepedia.exceptions.TriggerValueTypeException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownChannelException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;
import edu.stanford.braincat.rulepedia.model.Action;
import edu.stanford.braincat.rulepedia.model.Channel;
import edu.stanford.braincat.rulepedia.model.ObjectPool;
import edu.stanford.braincat.rulepedia.model.Value;

/**
 * Created by gcampagn on 5/18/15.
 */
public class GenericAction implements Action {
    private volatile Channel channel;
    private final String id;
    private final String text;
    private final String scriptBody;
    private Function script;
    private Scriptable thisArg;
    private final Map<String, Value> parameters;

    public GenericAction(Channel channel, String id, String text, String scriptBody, Map<String, Value> params) {
        super();
        this.id = id;
        this.text = text;
        this.channel = channel;
        this.scriptBody = scriptBody;

        parameters = params;
    }

    @Override
    public Channel getChannel() {
        return channel;
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
    public void resolve() throws UnknownObjectException {
        Channel newChannel = channel.resolve();
        if (!(newChannel instanceof GenericChannel))
            throw new UnknownObjectException(newChannel.getUrl());

        script = ((GenericChannel) newChannel).compileFunction(scriptBody);
        thisArg = new NativeObject();

        channel = newChannel;
    }

    @Override
    public void typeCheck(Map<String, Class<? extends Value>> context) throws TriggerValueTypeException {
        try {
            ((GenericChannelFactory) getChannel().getFactory()).typeCheckParameters(id, parameters, context);
            ((GenericChannelFactory) getChannel().getFactory()).updateGeneratesType(id, context);
        } catch (UnknownChannelException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public void execute(Context ctx, Map<String, Value> context) throws TriggerValueTypeException, UnknownObjectException, RuleExecutionException {
        Map<String, Value> resolved = new ArrayMap<>();
        for (Map.Entry<String, Value> e : parameters.entrySet()) {
            resolved.put(e.getKey(), e.getValue().resolve(context));
        }

        ScriptableObject result;

        try {
            NativeObject jsParameters = JSUtil.parametersToJavascript(resolved);
            ScriptableObject.putProperty(jsParameters, "url", getChannel().getUrl());
            result = (ScriptableObject) ((GenericChannel) channel).callFunction(script, thisArg,
                    jsParameters);
        } catch (Exception e) {
            throw new RuleExecutionException("Exception while evaluating action script", e);
        }

        GenericChannelFactory.parseActionResult(result).run(ctx);
    }

    @Override
    public String toHumanString() {
        return text;
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        return null;
    }
}
