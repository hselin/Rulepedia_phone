package edu.stanford.braincat.rulepedia.channels.android;

import edu.stanford.braincat.rulepedia.model.Contact;

/**
 * Created by gcampagn on 5/2/15.
 */
public class SMSContact extends Contact {
    public SMSContact(SMSContactFactory factory, String url) {
        super(factory, url);
    }

    @Override
    public String toHumanString() {
        return "a contact";
    }

    public String getAddress() {
        return getUrl().substring("sms:".length());
    }
}
