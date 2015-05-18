package edu.stanford.braincat.rulepedia.channels.generic;

import edu.stanford.braincat.rulepedia.exceptions.RuleExecutionException;

/**
 * Created by gcampagn on 5/18/15.
 */
public interface RuleRunnable {
    void run() throws RuleExecutionException;
}
