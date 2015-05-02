package edu.stanford.braincat.rulepedia.channels.sms;

import android.telephony.SmsMessage;

import edu.stanford.braincat.rulepedia.channels.SimpleEventTrigger;
import edu.stanford.braincat.rulepedia.channels.interfaces.Messaging;
import edu.stanford.braincat.rulepedia.model.Value;

/**
 * Created by gcampagn on 5/1/15.
 */
public class SMSMessageReceivedTrigger extends SimpleEventTrigger<SMSEventSource> {
    private SmsMessage receivedMessage;

    public SMSMessageReceivedTrigger(SMSChannel channel) {
        super(channel.getEventSource());
    }

    @Override
    public void update() {
        receivedMessage = getSource().getLastMessage();
    }

    @Override
    public String toHumanString() {
        return "a text message is received";
    }

    @Override
    public boolean producesValue(String name, Class<? extends Value> type) {
        switch (name) {
            case Messaging.SENDER:
                return type.equals(Value.Contact.class);

            case Messaging.MESSAGE:
                return type.equals(Value.Text.class);

            default:
                return false;
        }
    }

    @Override
    public Value getProducedValue(String name) {
        switch (name) {
            case Messaging.SENDER:
                return new Value.Contact(receivedMessage.getOriginatingAddress());

            case Messaging.MESSAGE:
                return new Value.Text(receivedMessage.getDisplayMessageBody());

            default:
                throw new RuntimeException("sms trigger does not produce " + name);
        }
    }
}
