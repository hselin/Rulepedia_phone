package edu.stanford.braincat.rulepedia.channels.sms;

import android.telephony.SmsMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.Map;

import edu.stanford.braincat.rulepedia.channels.SimpleEventTrigger;
import edu.stanford.braincat.rulepedia.channels.interfaces.Messaging;
import edu.stanford.braincat.rulepedia.exceptions.RuleExecutionException;
import edu.stanford.braincat.rulepedia.exceptions.TriggerValueTypeException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;
import edu.stanford.braincat.rulepedia.model.InternalObjectFactory;
import edu.stanford.braincat.rulepedia.model.ObjectPool;
import edu.stanford.braincat.rulepedia.model.Trigger;
import edu.stanford.braincat.rulepedia.model.Value;

/**
 * Created by gcampagn on 5/1/15.
 */
public class SMSMessageReceivedTrigger extends SimpleEventTrigger<SMSEventSource> {
    private SmsMessage receivedMessage;
    private final String contentContains;
    private final ObjectPool.Object senderMatches;

    public SMSMessageReceivedTrigger(SMSChannel channel, Value contentContains, Value senderMatches) throws UnknownObjectException {
        super(channel.getEventSource());
        if (contentContains != null)
            this.contentContains = ((Value.Text)contentContains.resolve(null)).getText();
        else
            this.contentContains = null;
        if (senderMatches != null)
            this.senderMatches = ((Value.DirectObject)senderMatches.resolve(null)).getObject();
        else
            this.senderMatches = null;
    }

    @Override
    public void update() {
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
                ObjectPool.Object sender = ObjectPool.get().getObject("sms:" + receivedMessage.getOriginatingAddress());
                if (!sender.equals(senderMatches)) {
                    receivedMessage = null;
                    return;
                }
            } catch (UnknownObjectException e) {
                receivedMessage = null;
                return;
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
        json.put(Trigger.OBJECT, InternalObjectFactory.PREDEFINED_PREFIX + SMSChannel.ID);
        json.put(Trigger.TRIGGER, Messaging.MESSAGE_RECEIVED);

        JSONArray jsonParams = new JSONArray();
        if (senderMatches != null) {
            try {
                jsonParams.put(new Value.Object(senderMatches.getUrl()).toJSON(Messaging.SENDER_MATCHES));
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
        if (contentContains != null)
            jsonParams.put(new Value.Text(contentContains).toJSON(Messaging.CONTENT_CONTAINS));
        json.put(Trigger.PARAMS, jsonParams);
        return json;
    }

    @Override
    public void typeCheck(Map<String, Class<? extends Value>> context) throws TriggerValueTypeException {
        context.put(Messaging.SENDER, Value.Object.class);
        context.put(Messaging.MESSAGE, Value.Text.class);
    }

    @Override
    public void updateContext(Map<String, Value> context) throws RuleExecutionException {
        try {
            context.put(Messaging.SENDER, new Value.DirectObject(ObjectPool.get().getObject("sms:" + receivedMessage.getOriginatingAddress())));
        } catch(UnknownObjectException e) {
            throw new RuntimeException(e);
        }

        context.put(Messaging.MESSAGE, new Value.Text(receivedMessage.getDisplayMessageBody(), true));
    }
}
