package edu.stanford.braincat.rulepedia.channels.sms;

import java.util.Map;

import edu.stanford.braincat.rulepedia.channels.interfaces.Messaging;
import edu.stanford.braincat.rulepedia.exceptions.TriggerValueTypeException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownChannelException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;
import edu.stanford.braincat.rulepedia.model.ChannelFactory;
import edu.stanford.braincat.rulepedia.model.ObjectPool;
import edu.stanford.braincat.rulepedia.model.Trigger;
import edu.stanford.braincat.rulepedia.model.Value;

/**
 * Created by gcampagn on 5/1/15.
 */
public class SMSTriggerFactory extends ChannelFactory<Trigger> {
    @Override
    public String getName() {
        return SMSChannel.ID;
    }

    @Override
    public Class<? extends Value> getParamType(String method, String name) throws UnknownChannelException, TriggerValueTypeException {
        switch (method) {
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

    @Override
    public Trigger createChannel(String method, ObjectPool.Object object, Map<String, Value> params) throws UnknownObjectException, UnknownChannelException {
        SMSChannel sms = (SMSChannel) object;

        switch (method) {
            case Messaging.MESSAGE_RECEIVED:
                return new SMSMessageReceivedTrigger(sms, params.get(Messaging.CONTENT_CONTAINS), params.get(Messaging.SENDER_MATCHES));

            default:
                throw new UnknownChannelException(method);
        }
    }
}
