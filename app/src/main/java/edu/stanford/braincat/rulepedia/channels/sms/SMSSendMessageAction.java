package edu.stanford.braincat.rulepedia.channels.sms;

import android.telephony.SmsManager;

import edu.stanford.braincat.rulepedia.exceptions.TriggerValueTypeException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;
import edu.stanford.braincat.rulepedia.model.Action;
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
    public void execute(Trigger trigger) throws UnknownObjectException {
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
}
