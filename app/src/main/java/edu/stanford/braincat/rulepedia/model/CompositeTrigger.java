package edu.stanford.braincat.rulepedia.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.stanford.braincat.rulepedia.events.EventSource;

/**
 * Created by gcampagn on 4/30/15.
 */
public abstract class CompositeTrigger implements Trigger {
    private final ArrayList<Trigger> children;

    protected CompositeTrigger(Collection<Trigger> children) {
        if (children.size() <= 1)
            throw new IllegalArgumentException("A composite trigger must have at least two arguments");

        this.children = new ArrayList<>();
        this.children.addAll(children);
    }

    protected abstract boolean compose(boolean t1, boolean t2);

    protected abstract String getHumanComposeOp();

    public void update() throws IOException {
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

    public boolean isFiring() {
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

    public static class Or extends CompositeTrigger {
        public Or(Collection<Trigger> children) {
            super(children);
        }

        @Override
        protected boolean compose(boolean t1, boolean t2) {
            return t1 || t2;
        }

        @Override
        protected String getHumanComposeOp() { return " or "; }
    }

    public static class And extends CompositeTrigger {
        public And(Collection<Trigger> children) {
            super(children);
        }

        @Override
        protected boolean compose(boolean t1, boolean t2) { return t1 && t2; }

        @Override
        protected String getHumanComposeOp() { return " and "; }
    }
}
