package edu.stanford.braincat.rulepedia.model;

/**
 * Created by gcampagn on 5/10/15.
 */
public class PlaceholderContact extends Contact {
    private final String text;

    public PlaceholderContact(ContactFactory factory, String url, String text) {
        super(factory, url);
        this.text = text;
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
