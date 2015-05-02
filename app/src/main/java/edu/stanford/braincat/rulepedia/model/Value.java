package edu.stanford.braincat.rulepedia.model;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import edu.stanford.braincat.rulepedia.exceptions.TriggerValueTypeException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;

/**
 * Created by gcampagn on 4/30/15.
 */
public abstract class Value {
    public void typeCheck(Class<? extends Value> expected) throws TriggerValueTypeException {
        if (!getClass().equals(expected))
            throw new TriggerValueTypeException("invalid value type, expected " + expected.getCanonicalName());
    }

    public void typeCheck(Trigger trigger, Class<? extends Value> expected) throws TriggerValueTypeException {
        typeCheck(expected);
    }


    public Value resolve(Trigger trigger) throws UnknownObjectException {
        return this;
    }

    public Value resolve() throws UnknownObjectException {
        return this;
    }

    public static class TriggerValue extends Value {
        public static final String ID = "trigger-value";

        private final String name;
        private final Class<? extends Value> type;

        public TriggerValue(String name, Class<? extends Value> type) throws TriggerValueTypeException {
            this.name = name;
            this.type = type;
        }

        // we don't override typeCheck() without a trigger, because if we don't
        // have a trigger to get a value from we should fail to typecheck
        @Override
        public void typeCheck(Trigger trigger, Class<? extends Value> expected) throws TriggerValueTypeException {
            if (!trigger.producesValue(name, type))
                throw new TriggerValueTypeException("trigger does not produce value " + name);
            if (!type.equals(expected))
                throw new TriggerValueTypeException("invalid value type, expected " + expected.getCanonicalName());
        }

        // we don't override resolve() without a trigger
        // we should have failed to typecheck anyway
        @Override
        public Value resolve(Trigger trigger) throws UnknownObjectException {
            return trigger.getProducedValue(name).resolve(trigger);
        }
    }

    public static class DirectObject extends Value {
        public static final String ID = "direct-object";

        private final ObjectPool.Object object;

        public DirectObject(ObjectPool.Object object) {
            this.object = object;
        }

        public String toString() {
            return object.getUrl();
        }

        public ObjectPool.Object getObject() {
            return object;
        }
    }

    public static class Object extends Value {
        public static final String ID = "object";

        private final String url;

        public Object(String rep) throws MalformedURLException {
            new URL(rep);
            url = rep;
        }

        public static Object fromString(String string) throws MalformedURLException {
            return new Object(string);
        }

        public String toString() {
            return url;
        }

        // FIXME: typechecking for objects? right now we would just say "object"

        @Override
        public Value resolve() throws UnknownObjectException {
            return new DirectObject(ObjectPool.get().getObject(url));
        }

        @Override
        public Value resolve(Trigger trigger) throws UnknownObjectException {
            return new DirectObject(ObjectPool.get().getObject(url)).resolve(trigger);
        }
    }

    public static class Text extends Value {
        public static final String ID = "text";

        private final String rep;

        public Text(String rep) {
            this.rep = rep;
        }

        public static Text fromString(String string) {
            return new Text(string);
        }

        public String toString() {
            return rep;
        }

        public String getText() {
            return rep;
        }
    }

    public static class Select extends Value {
        public static final String ID = "select";

        private final String rep;

        public Select(String rep) {
            this.rep = rep;
        }

        public static Select fromString(String string) {
            return new Select(string);
        }

        public String toString() {
            return rep;
        }

        public boolean typeCheck(Collection<String> allowed) {
            return allowed.contains(rep);
        }

        public String getSelect() {
            return rep;
        }
    }

    public static class Number extends Value {
        public static final String ID = "number";

        private final String rep; // FIXME

        public Number(String rep) {
            this.rep = rep;
        }

        public static Text fromString(String string) {
            return new Text(string);
        }

        public String toString() {
            return rep;
        }
    }
}
