package edu.stanford.braincat.rulepedia.channels.android;

import edu.stanford.braincat.rulepedia.model.Contact;
import edu.stanford.braincat.rulepedia.model.ContactFactory;

/**
 * Created by gcampagn on 5/2/15.
 */
public class SMSContactFactory extends ContactFactory {
    public static final String ID = "sms-contact";

    public SMSContactFactory() {
        super("sms:");
    }

    @Override
    public Contact create(String url) {
        return new SMSContact(this, url);
    }

    @Override
    public Contact createPlaceholder(String url) {
        return new Contact(this, url) {
            @Override
            public String toHumanString() {
                return "a contact";
            }
        };
    }

    @Override
    public String getName() {
        return ID;
    }
}
