package edu.stanford.braincat.rulepedia.model;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Map;

import edu.stanford.braincat.rulepedia.exceptions.TriggerValueTypeException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;

/**
 * Created by gcampagn on 4/30/15.
 */
public abstract class Value {
    public void typeCheck(@Nullable Map<String, Class<? extends Value>> context, Class<? extends Value> expected) throws TriggerValueTypeException {
        // anything can be coerced to text
        if (expected.equals(Value.Text.class))
            return;

        if (!getClass().equals(expected))
            throw new TriggerValueTypeException("invalid value type, expected " + expected.getCanonicalName());
    }

    public Value resolve(@Nullable Map<String, Value> context) throws TriggerValueTypeException, UnknownObjectException {
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

        public TriggerValue(String name, Class<? extends Value> type) {
            this.name = name;
            this.type = type;
        }

        // we don't override typeCheck() without a trigger, because if we don't
        // have a trigger to get a value from we should fail to typecheck
        @Override
        public void typeCheck(@Nullable Map<String, Class<? extends Value>> context, Class<? extends Value> expected) throws TriggerValueTypeException {
            if (context == null)
                throw new TriggerValueTypeException("context is not valid for trigger value");
            Class<? extends Value> produced = context.get(name);
            if (produced == null || !type.equals(produced))
                throw new TriggerValueTypeException("trigger does not produce value " + name);
            if (!expected.equals(Value.Text.class) && !type.equals(expected))
                throw new TriggerValueTypeException("invalid value type, expected " + expected.getCanonicalName());
        }

        @Override
        public Value resolve(@Nullable Map<String, Value> context) throws TriggerValueTypeException, UnknownObjectException {
            if (context == null)
                throw new TriggerValueTypeException("trigger value not allowed here");

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

        public DirectObject<K> resolve(Map<String, Value> context) {
            return this;
        }
    }

    private static abstract class Object<K extends ObjectPool.Object> extends Value {
        private final String url;

        public Object(String rep) throws URISyntaxException {
            new URI(rep);
            url = rep;
        }

        public String toString() {
            return url;
        }

        protected abstract K resolvePlaceholder(String url) throws UnknownObjectException;

        // FIXME: typechecking for objects? right now we would just say "channel"

        protected <P extends ObjectPool<K, ?>> Value resolve(@Nullable Map<String, Value> context, P pool) throws UnknownObjectException {
            K object = pool.getObject(url);
            if (object.isPlaceholder())
                return new DirectObject<>(resolvePlaceholder(url)).resolve(context);
            else
                return new DirectObject<>(object).resolve(context);
        }
    }

    public static class Contact extends Object<edu.stanford.braincat.rulepedia.model.Contact> {
        public static final String ID = "contact";

        public Contact(String url) throws URISyntaxException {
            super(url);
        }

        public static Contact fromString(String string) throws UnknownObjectException {
            try {
                // we should not need to trim here, but I screwed up during input so whatever
                return new Contact(string.trim());
            } catch(URISyntaxException e) {
                throw new UnknownObjectException(string);
            }
        }

        protected edu.stanford.braincat.rulepedia.model.Contact resolvePlaceholder(String url) throws UnknownObjectException {
            return ObjectDatabase.get().resolveContact(url);
        }

        @Override
        public Value resolve(@Nullable Map<String, Value> context) throws UnknownObjectException {
            return resolve(context, ContactPool.get());
        }
    }

    public static class Device extends Object<edu.stanford.braincat.rulepedia.model.Device> {
        public static final String ID = "device";

        public Device(String url) throws URISyntaxException {
            super(url);
        }

        public static Device fromString(String string) throws UnknownObjectException {
            try {
                // we should not need to trim here, but I screwed up during input so whatever
                return new Device(string.trim());
            } catch(URISyntaxException e) {
                throw new UnknownObjectException(string);
            }
        }

        protected edu.stanford.braincat.rulepedia.model.Device resolvePlaceholder(String url) throws UnknownObjectException {
            return ObjectDatabase.get().resolveDevice(url);
        }

        @Override
        public Value resolve(@Nullable Map<String, Value> context) throws UnknownObjectException {
            return resolve(context, DevicePool.get());
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
        public Value resolve(@Nullable Map<String, Value> context) throws TriggerValueTypeException, UnknownObjectException {
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
            } catch (NumberFormatException e) {
                try {
                    return new Number(Double.parseDouble(string));
                } catch (NumberFormatException e2) {
                    throw new TriggerValueTypeException("invalid numeric value");
                }
            }
        }

        public java.lang.Number getNumber() {
            return rep;
        }

        public String toString() {
            return rep.toString();
        }
    }

    public static class DirectPicture extends Value {
        private final String url;
        private final Bitmap rep;

        public DirectPicture(@Nullable String url, Bitmap rep) {
            this.url = url;
            this.rep = rep;
        }

        public Bitmap getPicture() {
            return rep;
        }

        @Override
        public String toString() {
            if (url != null) {
                return url;
            } else {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                rep.compress(Bitmap.CompressFormat.PNG, 100, stream);
                return "data:text/png;base64," + Uri.encode(Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT));
            }
        }
    }

    public static class Picture extends Value {
        public static final String ID = "picture";
        public static final String PLACEHOLDER = "https://rulepedia.stanford.edu/oid/placeholder/picture/any";

        private final String rep;

        public Picture(String rep) {
            this.rep = rep;
        }

        @Override
        public String toString() {
            return rep;
        }

        @Override
        public Value resolve(@Nullable Map<String, Value> context) throws UnknownObjectException {
            if (rep.equals(PLACEHOLDER))
                throw new UnknownObjectException(rep);

            return this;
        }

        public Value.DirectPicture toPicture(Context ctx) throws UnknownObjectException {
            if (rep.equals(PLACEHOLDER))
                throw new UnknownObjectException(rep);

            if (rep.startsWith("data:")) {
                String[] split = rep.split(",");

                if (split.length != 2 || !split[0].endsWith(";base64"))
                    throw new UnknownObjectException(rep);

                String uriDecoded = Uri.decode(split[1]);
                byte[] decoded = Base64.decode(uriDecoded, Base64.DEFAULT);

                return new DirectPicture(null, BitmapFactory.decodeByteArray(decoded, 0, decoded.length));
            }

            if (rep.startsWith(ContentResolver.SCHEME_CONTENT)) {
                if (ctx == null)
                    throw new UnknownObjectException(rep);

                try {
                    return new DirectPicture(rep, BitmapFactory.decodeStream(ctx.getContentResolver().openInputStream(Uri.parse(rep))));
                } catch(FileNotFoundException e) {
                    throw new UnknownObjectException(rep);
                }
            }

            try {
                URL url = new URL(rep);
                URLConnection connection = url.openConnection();
                try {
                    try (InputStream is = connection.getInputStream()) {
                        return new DirectPicture(rep, BitmapFactory.decodeStream(is));
                    }
                } finally {
                    if (connection instanceof HttpURLConnection)
                        ((HttpURLConnection) connection).disconnect();
                }
            } catch(IOException e) {
                // fall through
            }

            throw new UnknownObjectException(rep);
        }

        public static Picture fromString(String string) throws UnknownObjectException {
            if (string.equals(PLACEHOLDER) || string.startsWith("data:"))
                return new Picture(string);

            if (string.startsWith(ContentResolver.SCHEME_CONTENT)) {
                try {
                    ContentUris.parseId(Uri.parse(string));
                    return new Picture(string);
                } catch(NumberFormatException|UnsupportedOperationException e) {
                    throw new UnknownObjectException(string);
                }
            }

            try {
                new URL(string);
                return new Picture(string);
            } catch(MalformedURLException e){
                throw new UnknownObjectException(string);
            }
        }
    }
}
