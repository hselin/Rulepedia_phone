package edu.stanford.braincat.rulepedia.channels.android;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;
import edu.stanford.braincat.rulepedia.model.Contact;

/**
 * Created by gcampagn on 5/21/15.
 */
public class ContentProviderContact extends Contact {
    public ContentProviderContact(ContentProviderContactFactory factory, String url) {
        super(factory, url);
    }

    public String getPhoneNumber(Context ctx) throws UnknownObjectException {
        ContentResolver resolver = ctx.getContentResolver();

        try (Cursor contactCursor = resolver.query(Uri.parse(getUrl()),
                new String[]{ ContactsContract.Contacts.HAS_PHONE_NUMBER, ContactsContract.Contacts._ID },
                null, null, null)) {

            if (!contactCursor.moveToFirst())
                throw new UnknownObjectException(getUrl());

            int hasPhoneNumber = contactCursor.getInt(0);
            if (hasPhoneNumber == 0)
                return null;

            long contactId = contactCursor.getLong(1);

            try (Cursor dataCursor = resolver.query(ContactsContract.Data.CONTENT_URI,
                    new String[] { ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER },
                    ContactsContract.Data.CONTACT_ID + "=? and " +
                            ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "'",
                    new String[] { String.valueOf(contactId) }, null)) {
                dataCursor.moveToFirst();
                return dataCursor.getString(0);
            }
        }
    }

    @Override
    public String toHumanString() {
        return "a contact";
    }
}
