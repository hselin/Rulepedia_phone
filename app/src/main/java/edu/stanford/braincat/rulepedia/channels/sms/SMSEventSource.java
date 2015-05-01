package edu.stanford.braincat.rulepedia.channels.sms;

import android.telephony.SmsMessage;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.ArrayDeque;
import java.util.Queue;

import edu.stanford.braincat.rulepedia.events.EventSource;

/**
 * Created by gcampagn on 5/1/15.
 */
public class SMSEventSource implements EventSource {
    private Queue<SmsMessage> messages;

    public SMSEventSource(SMSChannel channel) {
        messages = new ArrayDeque<>();
    }

    public SmsMessage getLastMessage() {
        return messages.poll();
    }

    @Override
    public void install(Selector sel) throws IOException {
        // TODO
    }

    @Override
    public void uninstall() throws IOException {
        // TODO
    }

    @Override
    public long getTimeout() {
        return Long.MAX_VALUE;
    }

    @Override
    public boolean checkEvent() {
        return !messages.isEmpty();
    }

    @Override
    public void updateState() {
        // nothing to do here, trigger.update() handles it
    }
}
