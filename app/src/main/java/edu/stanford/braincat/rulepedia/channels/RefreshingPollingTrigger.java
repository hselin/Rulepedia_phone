package edu.stanford.braincat.rulepedia.channels;

import java.io.IOException;

import edu.stanford.braincat.rulepedia.exceptions.RuleExecutionException;

/**
 * Created by gcampagn on 5/1/15.
 */
public abstract class RefreshingPollingTrigger<K extends RefreshableObject> extends PollingTrigger {
    private K object;

    public RefreshingPollingTrigger(K object, long timeout) {
        super(timeout);
    }

    public K getObject() {
        return object;
    }

    @Override
    public void update() throws RuleExecutionException {
        try {
            object.refresh();
        } catch(IOException ioe) {
            throw new RuleExecutionException("IO exception while refreshing object", ioe);
        }
    }
}
