package edu.stanford.braincat.rulepedia.channels.email;

import java.util.Map;

import edu.stanford.braincat.rulepedia.channels.android.SMSChannel;
import edu.stanford.braincat.rulepedia.channels.android.SMSMessageReceivedTrigger;
import edu.stanford.braincat.rulepedia.channels.interfaces.Messaging;
import edu.stanford.braincat.rulepedia.channels.interfaces.SendMessageAction;
import edu.stanford.braincat.rulepedia.exceptions.TriggerValueTypeException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownChannelException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;
import edu.stanford.braincat.rulepedia.model.Action;
import edu.stanford.braincat.rulepedia.model.Channel;
import edu.stanford.braincat.rulepedia.model.ChannelFactory;
import edu.stanford.braincat.rulepedia.model.ChannelPool;
import edu.stanford.braincat.rulepedia.model.PlaceholderChannel;
import edu.stanford.braincat.rulepedia.model.Trigger;
import edu.stanford.braincat.rulepedia.model.Value;

/**
 * Created by gcampagn on 5/29/15.
 */
public class EmailChannelFactory extends ChannelFactory {
    public static final String ID = "email";

    public EmailChannelFactory() {
        super(ChannelPool.PREDEFINED_PREFIX + ID);
    }

    @Override
    public Action createAction(Channel channel, String method, Map<String, Value> params) throws
            UnknownObjectException, UnknownChannelException {
        switch (method) {
            case Messaging.SEND_MESSAGE:
                return new EmailSendMessageAction(channel, params.get("destination"), params.get("content"));

            default:
                throw new UnknownChannelException(method);
        }
    }

    @Override
    public Trigger createTrigger(Channel channel, String method, Map<String, Value> params)
            throws TriggerValueTypeException, UnknownObjectException, UnknownChannelException {
        throw new UnknownChannelException(method);
    }

    @Override
    public Class<? extends Value> getParamType(String method, String name) throws UnknownChannelException, TriggerValueTypeException {
        switch (method) {
            case Messaging.SEND_MESSAGE:
                switch (name) {
                    case Messaging.DESTINATION:
                        return Value.Contact.class;
                    case Messaging.MESSAGE:
                        return Value.Text.class;
                    default:
                        throw new TriggerValueTypeException("unknown parameter " + name);
                }

            default:
                throw new UnknownChannelException(method);
        }
    }

    @Override
    public Channel create(String url) throws UnknownObjectException {
        if (url.equals(getPrefix()))
            return new EmailChannel(this, url);
        else
            throw new UnknownObjectException(url);
    }

    @Override
    public Channel createPlaceholder(String url) {
        return new PlaceholderChannel(this, url, "email");
    }

    @Override
    public String getName() {
        return ID;
    }
}
