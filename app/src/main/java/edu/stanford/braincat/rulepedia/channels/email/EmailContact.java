package edu.stanford.braincat.rulepedia.channels.email;

import edu.stanford.braincat.rulepedia.model.Contact;

/**
 * Created by gcampagn on 5/29/15.
 */
public class EmailContact extends Contact {
    public EmailContact(EmailContactFactory factory, String url) {
        super(factory, url);
    }

    public String getEmail() {
        return getUrl().substring("mailto:".length());
    }

    @Override
    public String toHumanString() {
        return getEmail();
    }
}
