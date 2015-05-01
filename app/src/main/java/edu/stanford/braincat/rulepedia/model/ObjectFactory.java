package edu.stanford.braincat.rulepedia.model;

import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;

/**
 * Created by gcampagn on 4/30/15.
 */
public abstract class ObjectFactory {
    private final String prefix;

    protected ObjectFactory(String prefix) {
        this.prefix = prefix;
    }

    public boolean acceptsURL(String url) {
        return url.startsWith(prefix);
    }

    public abstract ObjectPool.Object create(String url) throws UnknownObjectException;

    public abstract String getName();
}
