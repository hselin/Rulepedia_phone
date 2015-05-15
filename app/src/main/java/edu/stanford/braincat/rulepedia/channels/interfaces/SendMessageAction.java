package edu.stanford.braincat.rulepedia.channels.interfaces;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import edu.stanford.braincat.rulepedia.exceptions.RuleExecutionException;
import edu.stanford.braincat.rulepedia.exceptions.TriggerValueTypeException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;
import edu.stanford.braincat.rulepedia.model.Action;
import edu.stanford.braincat.rulepedia.model.Channel;
import edu.stanford.braincat.rulepedia.model.Contact;
import edu.stanford.braincat.rulepedia.model.Value;

/**
 * Created by gcampagn on 5/14/15.
 */
public abstract class SendMessageAction implements Action {
    private volatile Channel channel;
    private final Value destination;
    private final Value message;

    public SendMessageAction(Channel channel, Value destination, Value message) {
        this.channel = channel;
        this.destination = destination;
        this.message = message;
    }

    protected void setChannel(Channel channel) {
        this.channel = channel;
    }

    public Channel getChannel() {
        return channel;
    }

    @Override
    public void typeCheck(Map<String, Class<? extends Value>> context) throws TriggerValueTypeException {
        destination.typeCheck(context, Value.Contact.class);
        message.typeCheck(context, Value.Text.class);
    }

    protected abstract void sendMessage(Context ctx, Contact destination, String message) throws UnknownObjectException, RuleExecutionException;

    @Override
    public void execute(Context ctx, Map<String, Value> context) throws RuleExecutionException, UnknownObjectException {
        Value.DirectObject resolvedDestination = (Value.DirectObject) destination.resolve(context);
        Value.Text resolvedMessage = (Value.Text) message.resolve(context);

        sendMessage(ctx, (Contact)resolvedDestination.getObject(), resolvedMessage.getText());
    }

    @Override
    public String toHumanString() {
        return "send a message to " + destination;
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(Action.OBJECT, channel.getUrl());
        json.put(Action.METHOD, Messaging.SEND_MESSAGE);

        JSONArray jsonParams = new JSONArray();
        jsonParams.put(destination.toJSON(Messaging.DESTINATION));
        jsonParams.put(message.toJSON(Messaging.MESSAGE));
        json.put(Action.PARAMS, jsonParams);
        return json;
    }
}
