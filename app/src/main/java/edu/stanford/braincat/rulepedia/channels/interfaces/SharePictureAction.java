package edu.stanford.braincat.rulepedia.channels.interfaces;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import edu.stanford.braincat.rulepedia.exceptions.RuleExecutionException;
import edu.stanford.braincat.rulepedia.exceptions.TriggerValueTypeException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;
import edu.stanford.braincat.rulepedia.model.Action;
import edu.stanford.braincat.rulepedia.model.Channel;
import edu.stanford.braincat.rulepedia.model.Contact;
import edu.stanford.braincat.rulepedia.model.ObjectPool;
import edu.stanford.braincat.rulepedia.model.Value;

/**
 * Created by gcampagn on 5/14/15.
 */
public abstract class SharePictureAction implements Action {
    private volatile Channel channel;
    private final Value destination;
    private final Value message;

    protected SharePictureAction(Channel channel, Value destination, Value message) {
        this.channel = channel;
        this.destination = destination;
        this.message = message;
    }

    protected void setChannel(Channel channel) {
        this.channel = channel;
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
        try {
            if (destination instanceof Value.Contact) {
                try {
                    Value.DirectObject resolved = (Value.DirectObject) destination.resolve(null);
                    if (resolved.getObject().isPlaceholder())
                        result.add(resolved.getObject());
                } catch (UnknownObjectException e) {
                    // nothing to do
                }
            } else if (destination instanceof Value.DirectObject) {
                if (((Value.DirectObject) destination).getObject().isPlaceholder())
                    result.add(((Value.DirectObject) destination).getObject());
            }
        } catch (TriggerValueTypeException e) {
            // nothing to do
        }

        return result;
    }

    @Override
    public void typeCheck(Map<String, Class<? extends Value>> context) throws TriggerValueTypeException {
        destination.typeCheck(context, Value.Contact.class);
        message.typeCheck(context, Value.Text.class);
    }

    protected abstract void sharePicture(Context ctx, Contact destination, Value.DirectPicture picture) throws UnknownObjectException, RuleExecutionException;

    @Override
    public void execute(Context ctx, Map<String, Value> context) throws TriggerValueTypeException, RuleExecutionException, UnknownObjectException {
        Value.DirectObject resolvedDestination = (Value.DirectObject) destination.resolve(context);
        Value.DirectPicture resolvedPicture = ((Value.Picture) message.resolve(context)).toPicture(ctx);

        sharePicture(ctx, (Contact) resolvedDestination.getObject(), resolvedPicture);
    }

    @Override
    public String toHumanString() {
        return "share a picture to " + destination;
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(Action.OBJECT, channel.getUrl());
        json.put(Action.METHOD, Messaging.SHARE_PICTURE);

        JSONArray jsonParams = new JSONArray();
        jsonParams.put(destination.toJSON(Messaging.DESTINATION));
        jsonParams.put(message.toJSON(Messaging.MESSAGE));
        json.put(Action.PARAMS, jsonParams);
        return json;
    }
}
