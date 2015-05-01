package edu.stanford.braincat.rulepedia.model;

import edu.stanford.braincat.rulepedia.exceptions.RuleExecutionException;

/**
 * Created by gcampagn on 4/30/15.
 */
public interface Action {
    void execute() throws RuleExecutionException;

    String toHumanString();
}
