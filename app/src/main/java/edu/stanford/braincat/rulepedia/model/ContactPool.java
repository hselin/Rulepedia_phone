package edu.stanford.braincat.rulepedia.model;

import edu.stanford.braincat.rulepedia.channels.android.ContentProviderContactFactory;
import edu.stanford.braincat.rulepedia.channels.android.TelephoneContactFactory;
import edu.stanford.braincat.rulepedia.channels.email.EmailContactFactory;

/**
 * Created by gcampagn on 5/9/15.
 */
public class ContactPool extends ObjectPool<Contact, ContactFactory> {
    public static final String KIND = "contact";
    public static final String PLACEHOLDER_PREFIX = ObjectPool.PLACEHOLDER_PREFIX + KIND + "/";

    private static final ContactPool instance = new ContactPool();

    public static ContactPool get() {
        return instance;
    }

    private ContactPool() {
        super(KIND);

        registerFactory(new TelephoneContactFactory());
        registerFactory(new EmailContactFactory());
        registerFactory(new ContentProviderContactFactory());
    }
}
