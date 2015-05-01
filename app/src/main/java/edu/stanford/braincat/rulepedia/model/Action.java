package edu.stanford.braincat.rulepedia.model;

import java.util.HashMap;
import java.util.Map;

import edu.stanford.braincat.rulepedia.exceptions.RuleExecutionException;

/**
 * Created by gcampagn on 4/30/15.
 */
public class Action {
    private final AbstractAction abstractAction;
    private final ObjectDatabase.Object object;
    private final HashMap<String, Value> params;

    public Action(AbstractAction abstractAction, ObjectDatabase.Object object, Map<String, Value> params) {
        this.abstractAction = abstractAction;
        this.object = object;
        this.params = new HashMap<>();
        this.params.putAll(params);
    }

    public void execute() throws RuleExecutionException {
        abstractAction.invoke(object, params);
    }

    public String toHumanString() {
        return abstractAction.toHumanString(object, params);
    }
}
