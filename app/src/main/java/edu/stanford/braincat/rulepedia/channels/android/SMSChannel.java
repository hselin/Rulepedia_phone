package edu.stanford.braincat.rulepedia.channels.android;

import edu.stanford.braincat.rulepedia.model.Channel;

/**
 * Created by gcampagn on 5/1/15.
 */
public class SMSChannel extends Channel {
    public static final String ID = "sms";

    private SMSEventSource eventSource;

    public SMSChannel(SMSChannelFactory factory, String url) {
        super(factory, url);
    }

    @Override
    public String toHumanString() {
        return "a text";
    }

    private void ensureEventSource() {
        if (eventSource != null)
            return;
        eventSource = new SMSEventSource();
    }

    public SMSEventSource getEventSource() {
        ensureEventSource();
        return eventSource;
    }
}
