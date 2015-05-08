package edu.stanford.braincat.rulepedia.channels.sms;

import java.util.Map;

import edu.stanford.braincat.rulepedia.channels.interfaces.Messaging;
import edu.stanford.braincat.rulepedia.exceptions.TriggerValueTypeException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownChannelException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;
import edu.stanford.braincat.rulepedia.model.Action;
import edu.stanford.braincat.rulepedia.model.InternalObjectFactory;
import edu.stanford.braincat.rulepedia.model.ObjectPool;
import edu.stanford.braincat.rulepedia.model.Trigger;
import edu.stanford.braincat.rulepedia.model.Value;

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

    @Override
    public Action createAction(String method, Map<String, Value> params) throws
            UnknownObjectException, UnknownChannelException {
        switch (method) {
            case Messaging.SEND_MESSAGE:
                return new SMSSendMessageAction(this, params.get("destination"), params.get("content"));

            default:
                throw new UnknownChannelException(method);
        }
    }

    @Override
    public Trigger createTrigger(String method, Map<String, Value> params)
            throws UnknownObjectException, UnknownChannelException {
        switch (method) {
            case Messaging.MESSAGE_RECEIVED:
                return new SMSMessageReceivedTrigger(this, params.get(Messaging.CONTENT_CONTAINS), params.get(Messaging.SENDER_MATCHES));

            default:
                throw new UnknownChannelException(method);
        }
    }

    @Override
    public Class<? extends Value> getParamType(String method, String name) throws UnknownChannelException, TriggerValueTypeException {
        switch (method) {
            case Messaging.SEND_MESSAGE:
                switch (name) {
                    case Messaging.DESTINATION:
                        return Value.Object.class;
                    case Messaging.MESSAGE:
                        return Value.Text.class;
                    default:
                        throw new TriggerValueTypeException("unknown parameter " + name);
                }

            case Messaging.MESSAGE_RECEIVED:
                switch (name) {
                    case Messaging.SENDER_MATCHES:
                        return Value.Object.class;
                    case Messaging.CONTENT_CONTAINS:
                        return Value.Text.class;
                    default:
                        throw new TriggerValueTypeException("unknown parameter " + name);
                }

            default:
                throw new UnknownChannelException(method);
        }
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
