package edu.stanford.braincat.rulepedia.channels.sms;

import edu.stanford.braincat.rulepedia.model.InternalObjectFactory;
import edu.stanford.braincat.rulepedia.model.ObjectPool;

/**
 * Created by gcampagn on 5/1/15.
 */
public class SMSChannel extends ObjectPool.Object {
    public static final String ID = "sms";

    private SMSEventSource eventSource;

    public SMSChannel() {
        super(InternalObjectFactory.PREDEFINED_PREFIX + ID);
    }

    @Override
    public String toHumanString() {
        return "a text";
    }

    @Override
    public String getType() {
        return ID;
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
