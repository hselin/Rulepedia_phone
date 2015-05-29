package edu.stanford.braincat.rulepedia.channels.android;

import android.content.Context;
import android.support.annotation.Nullable;
import android.telephony.SmsMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import edu.stanford.braincat.rulepedia.channels.SimpleEventTrigger;
import edu.stanford.braincat.rulepedia.channels.interfaces.Messaging;
import edu.stanford.braincat.rulepedia.exceptions.RuleExecutionException;
import edu.stanford.braincat.rulepedia.exceptions.TriggerValueTypeException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;
import edu.stanford.braincat.rulepedia.model.Channel;
import edu.stanford.braincat.rulepedia.model.Contact;
import edu.stanford.braincat.rulepedia.model.ContactPool;
import edu.stanford.braincat.rulepedia.model.ObjectPool;
import edu.stanford.braincat.rulepedia.model.Trigger;
import edu.stanford.braincat.rulepedia.model.Value;

/**
 * Created by gcampagn on 5/1/15.
 */
public class SMSMessageReceivedTrigger extends SimpleEventTrigger<SMSEventSource> {
    private volatile Channel channel;
    private SmsMessage receivedMessage;
    private final String contentContains;
    private volatile Contact senderMatches;

    public SMSMessageReceivedTrigger(Channel channel, @Nullable Value contentContains, @Nullable Value senderMatches) throws TriggerValueTypeException, UnknownObjectException {
        this.channel = channel;

        if (contentContains != null)
            this.contentContains = ((Value.Text) contentContains.resolve(null)).getText();
        else
            this.contentContains = null;
        if (senderMatches != null)
            this.senderMatches = (Contact) ((Value.DirectObject) senderMatches.resolve(null)).getObject();
        else
            this.senderMatches = null;
    }

    public Channel getChannel() {
        return channel;
    }

    public Collection<ObjectPool.Object> getPlaceholders() {
        Collection<ObjectPool.Object> result = new HashSet<>();

        Channel currentChannel = channel;
        if (currentChannel.isPlaceholder())
            result.add(currentChannel);
        Contact currentSenderMatcher = senderMatches;
        if (currentSenderMatcher != null && currentSenderMatcher.isPlaceholder())
            result.add(currentSenderMatcher);

        return result;
    }

    @Override
    public void update(Context ctx) {
        if (!getSource().checkEvent()) {
            receivedMessage = null;
            return;
        }

        receivedMessage = getSource().getLastMessage();
        assert receivedMessage != null;

        if (contentContains != null) {
            if (!receivedMessage.getDisplayMessageBody().contains(contentContains)) {
                receivedMessage = null;
                return;
            }
        }

        if (senderMatches != null) {
            try {
                Contact sender = ContactPool.get().getObject("sms:" + receivedMessage.getOriginatingAddress());
                if (!sender.equals(senderMatches)) {
                    receivedMessage = null;
                }
            } catch (UnknownObjectException e) {
                receivedMessage = null;
            }
        }
    }

    @Override
    public boolean isFiring() {
        return receivedMessage != null;
    }

    @Override
    public String toHumanString() {
        return "a text message is received";
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(Trigger.OBJECT, channel.getUrl());
        json.put(Trigger.TRIGGER, Messaging.MESSAGE_RECEIVED);

        JSONArray jsonParams = new JSONArray();
        if (senderMatches != null) {
            try {
                jsonParams.put(new Value.Contact(senderMatches.getUrl()).toJSON(Messaging.SENDER_MATCHES));
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        if (contentContains != null)
            jsonParams.put(new Value.Text(contentContains).toJSON(Messaging.CONTENT_CONTAINS));
        json.put(Trigger.PARAMS, jsonParams);
        return json;
    }

    @Override
    public void resolve() throws UnknownObjectException {
        Channel newChannel = channel.resolve();
        if (!(newChannel instanceof SMSChannel))
            throw new UnknownObjectException(newChannel.getUrl());
        Contact newSenderMatches = senderMatches != null ? senderMatches.resolve() : null;
        if (newSenderMatches != null && !(newSenderMatches instanceof SMSContact))
            throw new UnknownObjectException(newSenderMatches.getUrl());

        setSource(((SMSChannel) newChannel).getEventSource());
        channel = newChannel;
        senderMatches = newSenderMatches;
    }

    @Override
    public void typeCheck(Map<String, Class<? extends Value>> context) throws TriggerValueTypeException {
        context.put(Messaging.SENDER, Value.Contact.class);
        context.put(Messaging.MESSAGE, Value.Text.class);
    }

    @Override
    public void updateContext(Map<String, Value> context) throws RuleExecutionException {
        try {
            context.put(Messaging.SENDER, new Value.DirectObject<>(ContactPool.get().getObject("sms:" + receivedMessage.getOriginatingAddress())));
        } catch (UnknownObjectException e) {
            throw new RuntimeException(e);
        }

        context.put(Messaging.MESSAGE, new Value.Text(receivedMessage.getDisplayMessageBody(), true));
    }
}
