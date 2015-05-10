package edu.stanford.braincat.rulepedia.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.stanford.braincat.rulepedia.events.EventSource;
import edu.stanford.braincat.rulepedia.exceptions.RuleExecutionException;
import edu.stanford.braincat.rulepedia.exceptions.TriggerValueTypeException;

/**
 * Created by gcampagn on 4/30/15.
 */
public abstract class CompositeTrigger implements Trigger {
    public static final String COMBINATOR = "combinator";
    public static final String OPERANDS = "operands";

    private final ArrayList<Trigger> children;

    protected CompositeTrigger(Collection<Trigger> children) {
        if (children.size() <= 1)
            throw new IllegalArgumentException("A composite trigger must have at least two arguments");

        this.children = new ArrayList<>();
        this.children.addAll(children);
    }

    protected abstract boolean compose(boolean t1, boolean t2);

    protected abstract String getHumanComposeOp();

    protected abstract String getJSONComposeOp();

    public void update() throws RuleExecutionException {
        for (Trigger t : children)
            t.update();
    }

    @Override
    public Collection<EventSource> getEventSources() {
        Set<EventSource> sources = new HashSet<>();

        for (Trigger t : children) {
            sources.addAll(t.getEventSources());
        }

        return sources;
    }

    public boolean isFiring() throws RuleExecutionException {
        boolean v = compose(children.get(0).isFiring(), children.get(1).isFiring());
        for (int i = 2; i <= children.size(); i++)
            v = compose(v, children.get(i).isFiring());

        return v;
    }

    public String toHumanString() {
        StringBuilder builder = new StringBuilder();
        builder.append(children.get(0).toHumanString());
        for (int i = 1; i <= children.size(); i++) {
            builder.append(getHumanComposeOp());
            builder.append(children.get(i).toHumanString());
        }
        return builder.toString();
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(COMBINATOR, getJSONComposeOp());

        JSONArray jsonChildren = new JSONArray();
        for (Trigger t : children)
            jsonChildren.put(t.toJSON());
        json.put(OPERANDS, jsonChildren);

        return json;
    }

    @Override
    public void typeCheck(Map<String, Class<? extends Value>> context) throws TriggerValueTypeException {
        for (Trigger t : children)
            t.typeCheck(context);
    }

    @Override
    public void updateContext(Map<String, Value> context) throws RuleExecutionException {
        for (Trigger t : children)
            t.updateContext(context);
    }

    public static class Or extends CompositeTrigger {
        public static final String OP = "or";

        public Or(Collection<Trigger> children) {
            super(children);
        }

        @Override
        protected boolean compose(boolean t1, boolean t2) {
            return t1 || t2;
        }

        @Override
        protected String getHumanComposeOp() { return " or "; }

        @Override
        protected String getJSONComposeOp() { return OP; }
    }

    public static class And extends CompositeTrigger {
        public static final String OP = "and";

        public And(Collection<Trigger> children) {
            super(children);
        }

        @Override
        protected boolean compose(boolean t1, boolean t2) { return t1 && t2; }

        @Override
        protected String getHumanComposeOp() { return " and "; }

        @Override
        protected String getJSONComposeOp() { return OP; }
    }
}
