package edu.stanford.braincat.rulepedia.model;

import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;

/**
 * Created by gcampagn on 5/9/15.
 */
public abstract class Contact extends ObjectPool.Object<Contact, ContactFactory> {
    public Contact(ContactFactory factory, String url) {
        super(factory, url);
    }

    public Contact resolve() throws UnknownObjectException {
        return this;
    }
}
