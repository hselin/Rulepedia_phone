package edu.stanford.braincat.rulepedia.channels.android;

import java.util.Map;

import edu.stanford.braincat.rulepedia.exceptions.TriggerValueTypeException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownChannelException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;
import edu.stanford.braincat.rulepedia.model.Action;
import edu.stanford.braincat.rulepedia.model.Channel;
import edu.stanford.braincat.rulepedia.model.ChannelFactory;
import edu.stanford.braincat.rulepedia.model.ChannelPool;
import edu.stanford.braincat.rulepedia.model.Trigger;
import edu.stanford.braincat.rulepedia.model.Value;

/**
 * Created by gcampagn on 5/9/15.
 */
public class NotificationManagerChannelFactory extends ChannelFactory {
    public final static String ID = "notifications";
    public final static String POST_NOTIFICATION = "post-notification";
    public final static String TITLE = "title";
    public final static String TEXT = "text";

    public NotificationManagerChannelFactory() {
        super(ChannelPool.PREDEFINED_PREFIX + ID);
    }

    @Override
    public Class<? extends Value> getParamType(String method, String name) throws UnknownChannelException, TriggerValueTypeException {
        switch (method) {
            case POST_NOTIFICATION:
                switch (name) {
                    case TITLE:
                    case TEXT:
                        return Value.Text.class;
                    default:
                        throw new TriggerValueTypeException("unknown parameter " + name);
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
            case POST_NOTIFICATION:
                return new NotificationManagerPostNotificationAction(channel, params.get(TITLE), params.get(TEXT));
            default:
                throw new UnknownChannelException(method);
        }
    }

    @Override
    public Channel create(String url) throws UnknownObjectException {
        if (url.equals(getPrefix()))
            return new NotificationManagerChannel(this, url);
        else
            throw new UnknownObjectException(url);
    }

    @Override
    public Channel createPlaceholder(String url) {
        return new Channel(this, url) {
            @Override
            public String toHumanString() {
                return "notifications";
            }
        };
    }

    @Override
    public String getName() {
        return ID;
    }
}
