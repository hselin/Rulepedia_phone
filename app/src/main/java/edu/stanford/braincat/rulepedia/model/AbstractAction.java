package edu.stanford.braincat.rulepedia.model;

import java.util.Map;

import edu.stanford.braincat.rulepedia.exceptions.RuleExecutionException;

/**
 * Created by gcampagn on 4/30/15.
 */
public abstract class AbstractAction {
    public abstract void invoke(ObjectPool.Object on, Map<String, Value> params) throws RuleExecutionException;

    public abstract String toHumanString(ObjectPool.Object on, Map<String, Value> params);
}
