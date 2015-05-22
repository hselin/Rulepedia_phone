package edu.stanford.braincat.rulepedia.channels.android;

import android.content.ContentResolver;

import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;
import edu.stanford.braincat.rulepedia.model.Contact;
import edu.stanford.braincat.rulepedia.model.ContactFactory;
import edu.stanford.braincat.rulepedia.model.PlaceholderContact;

/**
 * Created by gcampagn on 5/21/15.
 */
public class ContentProviderContactFactory extends ContactFactory {
    public static final String ID = "content-provider";

    public ContentProviderContactFactory() {
        super(ContentResolver.SCHEME_CONTENT);
    }

    @Override
    public Contact create(String url) throws UnknownObjectException {
        return new ContentProviderContact(this, url);
    }

    @Override
    public Contact createPlaceholder(String url) {
        return new PlaceholderContact(this, url, "a contact");
    }

    @Override
    public String getName() {
        return ID;
    }
}
