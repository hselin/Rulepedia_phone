package edu.stanford.braincat.rulepedia.channels.sms;

import android.telephony.SmsManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.stanford.braincat.rulepedia.channels.interfaces.Messaging;
import edu.stanford.braincat.rulepedia.exceptions.RuleExecutionException;
import edu.stanford.braincat.rulepedia.exceptions.TriggerValueTypeException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;
import edu.stanford.braincat.rulepedia.model.Action;
import edu.stanford.braincat.rulepedia.model.InternalObjectFactory;
import edu.stanford.braincat.rulepedia.model.Trigger;
import edu.stanford.braincat.rulepedia.model.Value;

/**
 * Created by gcampagn on 5/1/15.
 */
public class SMSSendMessageAction implements Action {
    private final SMSChannel channel;
    private final Value destination;
    private final Value message;

    public SMSSendMessageAction(SMSChannel channel, Value destination, Value message) {
        this.channel = channel;
        this.destination = destination;
        this.message = message;
    }

    @Override
    public void typeCheck(Trigger trigger) throws TriggerValueTypeException {
        destination.typeCheck(trigger, Value.Object.class);
        message.typeCheck(trigger, Value.Text.class);
    }

    @Override
    public void execute(Trigger trigger) throws RuleExecutionException, UnknownObjectException {
        Value.DirectObject resolvedDestination = (Value.DirectObject) destination.resolve(trigger);
        Value.Text resolvedMessage = (Value.Text) message.resolve(trigger);

        SmsManager smsManager = SmsManager.getDefault();

        try {
            SMSContact smsContact = (SMSContact) resolvedDestination.getObject();
            smsManager.sendTextMessage(smsContact.getAddress(), null, resolvedMessage.getText(), null, null);
        } catch(ClassCastException e) {
            throw new UnknownObjectException(resolvedDestination.getObject().getUrl());
        }
    }

    @Override
    public String toHumanString() {
        return "send a message to " + destination;
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(Action.OBJECT, InternalObjectFactory.PREDEFINED_PREFIX + SMSChannel.ID);
        json.put(Action.METHOD, Messaging.SEND_MESSAGE);

        JSONArray jsonParams = new JSONArray();
        jsonParams.put(destination.toJSON(Messaging.DESTINATION));
        jsonParams.put(message.toJSON(Messaging.MESSAGE));
        json.put(Action.PARAMS, jsonParams);
        return json;
    }
}
