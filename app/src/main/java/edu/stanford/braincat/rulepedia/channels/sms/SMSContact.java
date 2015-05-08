package edu.stanford.braincat.rulepedia.channels.sms;

import java.util.Map;

import edu.stanford.braincat.rulepedia.exceptions.TriggerValueTypeException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownChannelException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;
import edu.stanford.braincat.rulepedia.model.Action;
import edu.stanford.braincat.rulepedia.model.ObjectPool;
import edu.stanford.braincat.rulepedia.model.Trigger;
import edu.stanford.braincat.rulepedia.model.Value;

/**
 * Created by gcampagn on 5/2/15.
 */
public class SMSContact extends ObjectPool.Object {
    public SMSContact(String url) {
        super(url);
    }

    @Override
    public String toHumanString() {
        return "a contact";
    }

    @Override
    public String getType() {
        return SMSContactFactory.ID;
    }

    @Override
    public Class<? extends Value> getParamType(String method, String name) throws UnknownChannelException, TriggerValueTypeException {
        throw new UnknownChannelException(method);
    }

    @Override
    public Trigger createTrigger(String method, Map<String, Value> params) throws UnknownObjectException, UnknownChannelException, TriggerValueTypeException {
        throw new UnknownChannelException(method);
    }

    @Override
    public Action createAction(String method, Map<String, Value> params) throws UnknownObjectException, UnknownChannelException, TriggerValueTypeException {
        throw new UnknownChannelException(method);
    }

    public String getAddress() {
        return getUrl().substring("sms:".length());
    }
}
