package edu.stanford.braincat.rulepedia.model;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;

/**
 * Created by gcampagn on 4/30/15.
 */
public class ObjectPool<K extends ObjectPool.Object, F extends ObjectPool.ObjectFactory<K>> {
    public static final String PREFIX = "https://rulepedia.stanford.edu/oid/";
    public static final String PREDEFINED_PREFIX = PREFIX + "predefined/";
    public static final String PLACEHOLDER_PREFIX = PREFIX + "placeholder/";

    public abstract static class Object<K extends Object, F extends ObjectFactory<K>> {
        private final F factory;
        private final String url;

        public Object(F factory, String url) {
            this.factory = factory;
            this.url = url;
        }

        public F getFactory() {
            return factory;
        }

        public boolean isPlaceholder() {
            return false;
        }

        public String getUrl() {
            return url;
        }

        public abstract String toHumanString();

        @Override
        public boolean equals(java.lang.Object object) {
            if (object == null)
                return false;
            if (object == this)
                return true;
            if (!(object instanceof Channel))
                return false;
            return url.equals(((Channel) object).getUrl());
        }

        @Override
        public int hashCode() {
            return url.hashCode();
        }
    }

    public abstract static class ObjectFactory<K extends ObjectPool.Object> {
        private final String prefix;

        public ObjectFactory(String prefix) {
            this.prefix = prefix;
        }

        public String getPrefix() {
            return prefix;
        }

        public boolean acceptsURL(String url) {
            return url.startsWith(prefix);
        }

        public abstract K create(String url) throws UnknownObjectException;

        public abstract K createPlaceholder(String url);

        public abstract String getName();
    }

    private final Pattern placeholderPattern;
    private final Map<String, K> knownObjects;
    private final Map<String, F> knownFactories;

    protected ObjectPool(String kind) {
        placeholderPattern = Pattern.compile("^https://rulepedia\\.stanford\\.edu/oid/placeholder/" + kind + "/([[a-z]\\-]+)$");
        knownObjects = new WeakHashMap<>();
        knownFactories = new HashMap<>();
    }

    protected void registerFactory(F factory) {
        knownFactories.put(factory.getName(), factory);
    }

    protected boolean hasFactory(String name) {
        return knownFactories.containsKey(name);
    }

    public synchronized K getObject(String url) throws UnknownObjectException {
        K existing = knownObjects.get(url);
        if (existing != null)
            return existing;

        // verify the url is valid
        try {
            new URL(url);
        } catch (MalformedURLException mue) {
            throw new UnknownObjectException(url);
        }

        for (ObjectFactory<K> factory : knownFactories.values()) {
            if (factory.acceptsURL(url)) {
                K newObject = factory.create(url);
                knownObjects.put(url, newObject);
                return newObject;
            }
        }

        Matcher m = placeholderPattern.matcher(url);
        if (m.matches()) {
            String subKind = m.group(1);
            F factory = knownFactories.get(subKind);
            if (factory != null)
                return factory.createPlaceholder(url);
        }

        throw new UnknownObjectException(url);
    }

}
