package edu.stanford.braincat.rulepedia.model;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by gcampagn on 4/30/15.
 */
public abstract class CompositeTrigger {
    private final ArrayList<Trigger> children;

    protected CompositeTrigger(Collection<Trigger> children) {
        if (children.size() <= 1)
            throw new IllegalArgumentException("A composite trigger must have at least two arguments");

        this.children = new ArrayList<Trigger>();
        this.children.addAll(children);
    }

    protected abstract boolean compose(boolean t1, boolean t2);

    public boolean isFiring() {
        boolean v = compose(children.get(0).isFiring(), children.get(1).isFiring());
        for (int i = 2; i <= children.size(); i++)
            v = compose(v, children.get(i).isFiring());

        return v;
    }

    public class Or extends CompositeTrigger {
        public Or(Collection<Trigger> children) {
            super(children);
        }

        protected boolean compose(boolean t1, boolean t2) {
            return t1 || t2;
        }
    }

    public class And extends CompositeTrigger {
        public And(Collection<Trigger> children) {
            super(children);
        }

        protected boolean compose(boolean t1, boolean t2) {
            return t1 && t2;
        }
    }
}
