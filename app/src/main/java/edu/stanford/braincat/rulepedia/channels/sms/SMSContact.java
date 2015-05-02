package edu.stanford.braincat.rulepedia.channels.sms;

import edu.stanford.braincat.rulepedia.model.ObjectPool;

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

    public String getAddress() {
        return getUrl().substring("sms:".length());
    }
}
