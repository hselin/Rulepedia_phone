package edu.stanford.braincat.rulepedia.channels.omlet;

import java.util.Map;

import edu.stanford.braincat.rulepedia.channels.interfaces.Messaging;
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
 * Created by gcampagn on 5/14/15.
 */
public class OmletChannelFactory extends ChannelFactory {
    public static final String ID = "omlet";

    public OmletChannelFactory() {
        super(ChannelPool.PREDEFINED_PREFIX + ID);
    }

    @Override
    public Class<? extends Value> getParamType(String method, String name) throws UnknownChannelException, TriggerValueTypeException {
        switch (method) {
            case Messaging.SEND_MESSAGE:
                switch (name) {
                    case Messaging.MESSAGE:
                        return Value.Text.class;
                    case Messaging.DESTINATION:
                        return Value.Contact.class;
                    default:
                        throw new TriggerValueTypeException("invalid param " + name);
                }
            case Messaging.SHARE_PICTURE:
                switch (name) {
                    case Messaging.MESSAGE:
                        return Value.Picture.class;
                    case Messaging.DESTINATION:
                        return Value.Contact.class;
                    default:
                        throw new TriggerValueTypeException("invalid param " + name);
                }
            default:
                throw new UnknownChannelException(method);
        }
    }

    @Override
    public Trigger createTrigger(Channel channel, String method, Map<String, Value> params) throws UnknownObjectException, UnknownChannelException, TriggerValueTypeException {
        throw new UnknownChannelException(method);
    }

    @Override
    public Action createAction(Channel channel, String method, Map<String, Value> params) throws UnknownObjectException, UnknownChannelException, TriggerValueTypeException {
        switch (method) {
            case Messaging.SEND_MESSAGE:
                return new OmletSendMessageAction(channel, params.get(Messaging.DESTINATION), params.get(Messaging.MESSAGE));
            case Messaging.SHARE_PICTURE:
                return new OmletSharePictureAction(channel, params.get(Messaging.DESTINATION), params.get(Messaging.MESSAGE));
            default:
                throw new UnknownChannelException(method);
        }
    }

    @Override
    public Channel create(String url) throws UnknownObjectException {
        return new OmletChannel(this, url);
    }

    @Override
    public Channel createPlaceholder(String url) {
        return new PlaceholderChannel(this, url, "Omlet");
    }

    @Override
    public String getName() {
        return ID;
    }
}
