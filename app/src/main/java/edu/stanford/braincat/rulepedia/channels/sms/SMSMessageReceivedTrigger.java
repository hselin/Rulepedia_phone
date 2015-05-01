package edu.stanford.braincat.rulepedia.channels.sms;

import android.telephony.SmsMessage;

import edu.stanford.braincat.rulepedia.channels.SimpleEventTrigger;

/**
 * Created by gcampagn on 5/1/15.
 */
public class SMSMessageReceivedTrigger extends SimpleEventTrigger<SMSEventSource> {
    private SmsMessage receivedMessage;

    public SMSMessageReceivedTrigger(SMSChannel channel) {
        super(new SMSEventSource(channel));
    }

    @Override
    public void update() {
        receivedMessage = getSource().getLastMessage();
    }

    @Override
    public String toHumanString() {
        return "a text message is received";
    }
}
