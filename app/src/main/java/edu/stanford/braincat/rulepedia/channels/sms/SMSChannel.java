package edu.stanford.braincat.rulepedia.channels.sms;

import edu.stanford.braincat.rulepedia.model.ObjectPool;

/**
 * Created by gcampagn on 5/1/15.
 */
public class SMSChannel extends ObjectPool.Object {
    public static final String ID = "sms";

    public SMSChannel() {
        super("rulepedia:predefined/object/sms");
    }

    @Override
    public String toHumanString() {
        return "a text";
    }

    @Override
    public String getType() {
        return ID;
    }
}
