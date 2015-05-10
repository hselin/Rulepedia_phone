package edu.stanford.braincat.rulepedia.channels.time;

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
 * Created by gcampagn on 5/9/15.
 */
public class Timer extends ObjectPool.Object {
    public final static String ID = "timer";
    public final static String ELAPSED = "elapsed";
    public final static String INTERVAL = "interval";

    public Timer() {
        super(InternalObjectFactory.PREDEFINED_PREFIX + ID);
    }

    @Override
    public String toHumanString() {
        return "a timer";
    }

    @Override
    public String getType() {
        return ID;
    }

    @Override
    public Class<? extends Value> getParamType(String method, String name) throws UnknownChannelException, TriggerValueTypeException {
        switch (method) {
            case ELAPSED:
                switch (name) {
                    case INTERVAL:
                        return Value.Number.class;
                    default:
                        throw new TriggerValueTypeException("unknown parameter " + name);
                }

            default:
                throw new UnknownChannelException(method);
        }
    }

    @Override
    public Trigger createTrigger(String method, Map<String, Value> params) throws UnknownObjectException, UnknownChannelException, TriggerValueTypeException {
        switch (method) {
            case Messaging.MESSAGE_RECEIVED:
                return new TimeTrigger(this, params.get(INTERVAL));
            default:
                throw new UnknownChannelException(method);
        }
    }

    @Override
    public Action createAction(String method, Map<String, Value> params) throws UnknownObjectException, UnknownChannelException, TriggerValueTypeException {
        throw new UnknownChannelException(method);
    }
}
