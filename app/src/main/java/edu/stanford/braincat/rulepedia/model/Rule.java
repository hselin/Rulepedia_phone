package edu.stanford.braincat.rulepedia.model;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import edu.stanford.braincat.rulepedia.BuildConfig;
import edu.stanford.braincat.rulepedia.events.EventSource;
import edu.stanford.braincat.rulepedia.exceptions.RuleExecutionException;
import edu.stanford.braincat.rulepedia.exceptions.TriggerValueTypeException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;
import edu.stanford.braincat.rulepedia.service.RuleExecutorThread;

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
    private boolean installed;
    private volatile String id;
    private volatile int priority;
    private volatile boolean enabled;

    public Rule(String name, String description, Trigger trigger, Collection<Action> actions) {
        if (actions.size() == 0)
            throw new IllegalArgumentException("a rule must have at least one action");

        this.name = name;
        this.trigger = trigger;
        this.actions = new ArrayList<>();
        this.actions.addAll(actions);
        this.enabled = false;
        this.description = description;
        this.id = null;
        this.installed = false;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isInstalled() {
        if (BuildConfig.DEBUG && !(Thread.currentThread() instanceof RuleExecutorThread))
            throw new AssertionError();
        return installed;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setInstalled(boolean installed) {
        if (BuildConfig.DEBUG && !(Thread.currentThread() instanceof RuleExecutorThread))
            throw new AssertionError();
        this.installed = installed;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        if (this.id != null)
            throw new IllegalStateException("cannot set id twice");
        this.id = id;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Trigger getTrigger() {
        return trigger;
    }

    public Collection<Action> getActions() {
        return Collections.unmodifiableList(actions);
    }

    public Collection<ObjectPool.Object> getPlaceholders() {
        Collection<ObjectPool.Object> result = new HashSet<>();

        result.addAll(trigger.getPlaceholders());
        for (Action a : actions)
            result.addAll(a.getPlaceholders());

        return result;
    }

    public Collection<EventSource> getEventSources() {
        return trigger.getEventSources();
    }

    private static void collectTriggerChannels(Trigger trigger, Collection<Channel> ctx) {
        if (trigger instanceof CompositeTrigger) {
            for (Trigger t : ((CompositeTrigger) trigger).getChildren())
                collectTriggerChannels(t, ctx);
        } else {
            ctx.add(trigger.getChannel());
        }
    }

    public Collection<Channel> getChannels() {
        Collection<Channel> channels = new HashSet<>();
        collectTriggerChannels(trigger, channels);
        for (Action a : actions)
            channels.add(a.getChannel());

        return channels;
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

    public void updateTrigger(Context ctx) throws RuleExecutionException {
        if (!enabled)
            return;
        trigger.update(ctx);
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
        } catch (UnknownObjectException | TriggerValueTypeException e) {
            throw new RuleExecutionException(e);
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

        if (id != null)
            json.put("id", id);

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
