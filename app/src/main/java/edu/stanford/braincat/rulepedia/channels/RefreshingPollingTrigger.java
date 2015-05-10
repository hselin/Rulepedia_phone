package edu.stanford.braincat.rulepedia.channels;

import edu.stanford.braincat.rulepedia.model.Channel;

/**
 * Created by gcampagn on 5/1/15.
 */
public abstract class RefreshingPollingTrigger<K extends Channel & RefreshableObject> extends PollingTrigger {
    private K object;

    public RefreshingPollingTrigger(K object, long timeout) {
        super(timeout);
    }

    public K getObject() {
        return object;
    }
}
