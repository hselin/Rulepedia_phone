package edu.stanford.braincat.rulepedia.model;

import edu.stanford.braincat.rulepedia.exceptions.RuleExecutionException;
import edu.stanford.braincat.rulepedia.exceptions.TriggerValueTypeException;

/**
 * Created by gcampagn on 4/30/15.
 */
public interface Action {
    void typeCheck(Trigger trigger) throws TriggerValueTypeException;

    void execute(ObjectPool pool, Trigger trigger) throws RuleExecutionException;

    String toHumanString();
}
