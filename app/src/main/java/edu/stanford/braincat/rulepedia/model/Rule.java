package edu.stanford.braincat.rulepedia.model;

import java.util.ArrayList;
import java.util.Collection;

import edu.stanford.braincat.rulepedia.events.EventSource;
import edu.stanford.braincat.rulepedia.exceptions.RuleExecutionException;

/**
 * Created by gcampagn on 4/30/15.
 */
public class Rule {
    private final String name;
    private final Trigger trigger;
    private final ArrayList<Action> actions;
    private final int priority;

    public Rule(String name, Trigger trigger, Collection<Action> actions, int priority) {
        if (actions.size() == 0)
            throw new IllegalArgumentException("a rule must have at least one action");

        this.name = name;
        this.trigger = trigger;
        this.actions = new ArrayList<Action>();
        this.actions.addAll(actions);
        this.priority = priority;
    }

    private String getName() {
        return name;
    }

    public int getPriority() {
        return priority;
    }

    public Collection<EventSource> getEventSources() {
        return trigger.getEventSources();
    }

    public void updateTrigger() throws RuleExecutionException {
        trigger.update();
    }

    public boolean isFiring() throws RuleExecutionException {
        return trigger.isFiring();
    }

    public void fire() throws RuleExecutionException {
        for (Action a : actions)
            a.execute();
    }

    public String toHumanString() {
        StringBuilder builder = new StringBuilder();
        builder.append("when ");
        builder.append(trigger.toHumanString());
        builder.append(", do ");
        builder.append(actions.get(0).toHumanString());
        for (int i = 1; i < actions.size(); i++) {
            builder.append(" and ");
            builder.append(actions.get(i).toHumanString());
        }

        return builder.toString();
    }
}
