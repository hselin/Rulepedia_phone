package edu.stanford.braincat.rulepedia.model;

/**
 * Created by gcampagn on 5/9/15.
 */
public abstract class ContactFactory extends ObjectPool.ObjectFactory<Contact> {
    public ContactFactory(String prefix) {
        super(prefix);
    }
}
