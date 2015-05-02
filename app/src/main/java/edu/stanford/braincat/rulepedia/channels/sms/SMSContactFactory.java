package edu.stanford.braincat.rulepedia.channels.sms;

import edu.stanford.braincat.rulepedia.model.ObjectFactory;
import edu.stanford.braincat.rulepedia.model.ObjectPool;

/**
 * Created by gcampagn on 5/2/15.
 */
public class SMSContactFactory extends ObjectFactory {
    public static final String ID = "sms-contact";

    public SMSContactFactory() {
        super("sms:");
    }

    @Override
    public ObjectPool.Object create(String url) {
        return new SMSContact(url);
    }

    @Override
    public String getName() {
        return ID;
    }
}
