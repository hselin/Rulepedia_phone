package edu.stanford.braincat.rulepedia.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

import edu.stanford.braincat.rulepedia.exceptions.TriggerValueTypeException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;

/**
 * Created by gcampagn on 4/30/15.
 */
public abstract class Value {
    public void typeCheck(Map<String, Class<? extends Value>> context, Class<? extends Value> expected) throws TriggerValueTypeException {
        // anything can be coerced to text
        if (expected.equals(Value.Text.class))
            return;

        if (!getClass().equals(expected))
            throw new TriggerValueTypeException("invalid value type, expected " + expected.getCanonicalName());
    }

    public Value resolve(Map<String, Value> context) throws UnknownObjectException {
        return this;
    }

    public JSONObject toJSON(String name) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("name", name);
        json.put("value", toString());
        return json;
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
        public void typeCheck(Map<String, Class<? extends Value>> context, Class<? extends Value> expected) throws TriggerValueTypeException {
            if (context == null)
                throw new TriggerValueTypeException("context is not valid for trigger value");
            Class<? extends Value> produced = context.get(name);
            if (produced == null || !type.equals(produced))
                throw new TriggerValueTypeException("trigger does not produce value " + name);
            if (!expected.equals(Value.Text.class) && !type.equals(expected))
                throw new TriggerValueTypeException("invalid value type, expected " + expected.getCanonicalName());
        }

        // we don't check context != null
        // we should have failed to typecheck anyway
        @Override
        public Value resolve(Map<String, Value> context) throws UnknownObjectException {
            return context.get(name).resolve(context);
        }

        @Override
        public JSONObject toJSON(String paramName) throws JSONException {
            JSONObject json = new JSONObject();
            json.put("name", paramName);
            json.put("trigger-value", name);
            return json;
        }
    }

    public static class DirectObject<K extends ObjectPool.Object> extends Value {
        public static final String ID = "direct-object";

        private final K object;

        public DirectObject(K object) {
            this.object = object;
        }

        public String toString() {
            return object.getUrl();
        }

        public K getObject() {
            return object;
        }
    }

    private static class Object<K extends ObjectPool.Object> extends Value {
        private final String url;

        public Object(String rep) throws MalformedURLException {
            new URL(rep);
            url = rep;
        }

        public String toString() {
            return url;
        }

        // FIXME: typechecking for objects? right now we would just say "channel"

        protected <P extends ObjectPool<K, ?>> Value resolve(Map<String, Value> context, P pool) throws UnknownObjectException {
            K object = pool.getObject(url);
            if (object.isPlaceholder())
                throw new UnknownObjectException(url);
            return new DirectObject<>(object).resolve(context);
        }
    }

    public static class Contact extends Object<edu.stanford.braincat.rulepedia.model.Contact> {
        public static final String ID = "contact";

        public Contact(String url) throws MalformedURLException {
            super(url);
        }

        public static Contact fromString(String string) throws MalformedURLException {
            return new Contact(string);
        }

        @Override
        public Value resolve(Map<String, Value> context) throws UnknownObjectException {
            return resolve(context, ContactPool.get());
        }
    }

    public static class Text extends Value {
        public static final String ID = "text";

        private final String rep;
        private final boolean resolved;

        public Text(String rep, boolean resolved) {
            this.rep = rep;
            this.resolved = resolved;
        }

        public Text(String rep) {
            this(rep, false);
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

        @Override
        public Value resolve(Map<String, Value> context) throws UnknownObjectException {
            if (resolved)
                return this;
            if (context == null)
                return this;

            if (!rep.contains("{{"))
                return new Text(rep, true);

            String acc = rep;
            for (Map.Entry<String, Value> entry : context.entrySet()) {
                acc = acc.replace("{{" + entry.getKey() + "}}", entry.getValue().resolve(context).toString());
            }

            return new Text(acc, true);
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

        private final java.lang.Number rep;

        public Number(java.lang.Number rep) {
            this.rep = rep;
        }

        public static Number fromString(String string) throws TriggerValueTypeException {
            try {
                return new Number(Integer.parseInt(string));
            } catch(NumberFormatException e) {
                try {
                    return new Number(Double.parseDouble(string));
                } catch(NumberFormatException e2) {
                    throw new TriggerValueTypeException("invalid numeric value");
                }
            }
        }

        public java.lang.Number getNumber() { return rep; }

        public String toString() {
            return rep.toString();
        }
    }
}
