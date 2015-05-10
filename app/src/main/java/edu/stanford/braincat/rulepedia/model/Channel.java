package edu.stanford.braincat.rulepedia.model;

import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;

/**
 * Created by gcampagn on 4/30/15.
 */
public abstract class Channel extends ObjectPool.Object<Channel, ChannelFactory> {
    protected Channel(ChannelFactory factory, String url) {
        super(factory, url);
    }

    public Channel resolve() throws UnknownObjectException {
        return this;
    }
}
