package edu.stanford.braincat.rulepedia.model;

import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;

/**
 * Created by gcampagn on 5/10/15.
 */
public class PlaceholderContact extends Contact {
    private final String text;

    public PlaceholderContact(ContactFactory factory, String url, String text) {
        super(factory, url);
        this.text = text;
    }

    public Contact resolve() throws UnknownObjectException {
        throw new UnknownObjectException(getUrl());
    }

    @Override
    public boolean isPlaceholder() {
        return true;
    }

    @Override
    public String toHumanString() {
        return text;
    }
}
