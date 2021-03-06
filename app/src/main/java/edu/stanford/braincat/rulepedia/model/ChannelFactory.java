package edu.stanford.braincat.rulepedia.model;

import java.util.Map;

import edu.stanford.braincat.rulepedia.exceptions.TriggerValueTypeException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownChannelException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;

/**
 * Created by gcampagn on 4/30/15.
 */
public abstract class ChannelFactory extends ObjectPool.ObjectFactory<Channel> {
    protected ChannelFactory(String prefix) {
        super(prefix);
    }

    public abstract Class<? extends Value> getParamType(String method, String name) throws UnknownChannelException, TriggerValueTypeException;

    public abstract Trigger createTrigger(Channel channel, String method, Map<String, Value> params)
            throws UnknownObjectException, UnknownChannelException, TriggerValueTypeException;

    public abstract Action createAction(Channel channel, String method, Map<String, Value> params)
            throws UnknownObjectException, UnknownChannelException, TriggerValueTypeException;
}
