package edu.stanford.braincat.rulepedia.model;

import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;

/**
 * Created by gcampagn on 5/10/15.
 */
public class PlaceholderChannel extends Channel {
    private final String text;

    public PlaceholderChannel(ChannelFactory factory, String url, String text) {
        super(factory, url);
        this.text = text;
    }

    public Channel resolve() throws UnknownObjectException {
        return ObjectDatabase.get().resolveChannel(getUrl());
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
