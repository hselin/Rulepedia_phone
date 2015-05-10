package edu.stanford.braincat.rulepedia.model;

/**
 * Created by gcampagn on 5/9/15.
 */
public abstract class Contact extends ObjectPool.Object<Contact, ContactFactory> {
    public Contact(ContactFactory factory, String url) {
        super(factory, url);
    }
}
