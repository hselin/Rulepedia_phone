package edu.stanford.braincat.rulepedia.model;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import edu.stanford.braincat.rulepedia.events.EventSource;
import edu.stanford.braincat.rulepedia.exceptions.RuleExecutionException;
import edu.stanford.braincat.rulepedia.exceptions.TriggerValueTypeException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;

/**
 * Created by gcampagn on 4/30/15.
 */
public class Rule {
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String TRIGGER = "trigger";
    public static final String ACTIONS = "actions";
    public static final String ENABLED = "enabled";

    private final String name;
    private final String description;
    private final Trigger trigger;
    private final ArrayList<Action> actions;
    private final String id;
    private int priority;
    private volatile boolean enabled;

    public Rule(String name, String description, Trigger trigger, Collection<Action> actions) {
        if (actions.size() == 0)
            throw new IllegalArgumentException("a rule must have at least one action");

        this.name = name;
        this.trigger = trigger;
        this.actions = new ArrayList<Action>();
        this.actions.addAll(actions);
        this.enabled = false;
        this.description = description;
        this.id = "1231231231323123123";
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {return description;}

    public String getID() {return id;}

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Collection<EventSource> getEventSources() {
        return trigger.getEventSources();
    }

    public void typeCheck() throws TriggerValueTypeException {
        Map<String, Class<? extends Value>> context = new HashMap<>();
        trigger.typeCheck(context);
        for (Action a : actions)
            a.typeCheck(context);
    }

    public void resolve() throws UnknownObjectException {
        trigger.resolve();
        for (Action a : actions)
            a.resolve();
    }

    public void updateTrigger() throws RuleExecutionException {
        if (!enabled)
            return;
        trigger.update();
    }

    public boolean isFiring() throws RuleExecutionException {
        return enabled && trigger.isFiring();
    }

    public void fire(Context ctx) throws RuleExecutionException {
        if (!enabled)
            throw new IllegalStateException("rule not enabled");

        try {
            Map<String, Value> context = new HashMap<>();

            trigger.updateContext(context);
            for (Action a : actions)
                a.execute(ctx, context);
        } catch(UnknownObjectException uoe) {
            throw new RuleExecutionException(uoe);
        }
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("name", name);
        json.put("description", description);
        json.put("trigger", trigger.toJSON());

        JSONArray jsonActions = new JSONArray();
        for (Action a : actions) {
            jsonActions.put(a.toJSON());
        }

        json.put("actions", jsonActions);
        json.put("enabled", enabled);

        return json;
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
