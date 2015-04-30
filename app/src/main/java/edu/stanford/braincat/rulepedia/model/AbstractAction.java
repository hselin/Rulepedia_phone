package edu.stanford.braincat.rulepedia.model;

import java.util.Map;

import edu.stanford.braincat.rulepedia.exceptions.RuleExecutionException;

/**
 * Created by gcampagn on 4/30/15.
 */
public abstract class AbstractAction {
    public abstract void invoke(ObjectDatabase.Object on, Map<String, Value> params) throws RuleExecutionException;
}
