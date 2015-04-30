package edu.stanford.braincat.rulepedia.model;

import edu.stanford.braincat.rulepedia.exceptions.RuleExecutionException;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by gcampagn on 4/30/15.
 */
public class Rule {
    private final Trigger trigger;
    private final ArrayList<Action> actions;

    public Rule(Trigger trigger, Collection<Action> actions) {
        this.trigger = trigger;
        this.actions = new ArrayList<Action>();
        this.actions.addAll(actions);
    }

    public boolean isFiring() {
        return trigger.isFiring();
    }

    public void fire() throws RuleExecutionException {
        for (Action a : actions)
            a.execute();
    }
}
