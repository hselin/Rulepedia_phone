package edu.stanford.braincat.rulepedia.channels.android;

import edu.stanford.braincat.rulepedia.model.Contact;
import edu.stanford.braincat.rulepedia.model.ContactFactory;
import edu.stanford.braincat.rulepedia.model.PlaceholderContact;

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
        return new PlaceholderContact(this, url, "a phone number");
    }

    @Override
    public String getName() {
        return ID;
    }
}
