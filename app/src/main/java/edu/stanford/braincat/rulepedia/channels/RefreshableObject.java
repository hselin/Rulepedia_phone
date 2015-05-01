package edu.stanford.braincat.rulepedia.channels;

import java.io.IOException;

import edu.stanford.braincat.rulepedia.model.ObjectPool;

/**
 * Created by gcampagn on 5/1/15.
 */
public abstract class RefreshableObject extends ObjectPool.Object {
    protected RefreshableObject(String url) {
        super(url);
    }

    public abstract void refresh() throws IOException;
}
