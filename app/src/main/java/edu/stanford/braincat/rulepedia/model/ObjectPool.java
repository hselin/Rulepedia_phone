package edu.stanford.braincat.rulepedia.model;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.stanford.braincat.rulepedia.channels.omdb.OMDBObjectFactory;
import edu.stanford.braincat.rulepedia.channels.sms.SMSContactFactory;
import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;

/**
 * Created by gcampagn on 4/30/15.
 */
public class ObjectPool {
    private final Map<String, Object> knownObjects;
    private final ArrayList<ObjectFactory> knownFactories;

    private static final ObjectPool instance = new ObjectPool();

    public static ObjectPool get() {
        return instance;
    }

    private ObjectPool() {
        knownObjects = new HashMap<>();
        knownFactories = new ArrayList<>();

        registerFactory(new InternalObjectFactory());
        registerFactory(new SMSContactFactory());
        registerFactory(new OMDBObjectFactory());
    }

    public void registerFactory(ObjectFactory factory) {
        knownFactories.add(factory);
    }

    public Object getObject(String url) throws UnknownObjectException {
        Object existing = knownObjects.get(url);
        if (existing != null)
            return existing;

        // verify the url is valid
        try {
            new URL(url);
        } catch(MalformedURLException mue) {
            throw new UnknownObjectException(url);
        }

        for (ObjectFactory factory : knownFactories) {
            if (factory.acceptsURL(url))
                return factory.create(url);
        }

        throw new UnknownObjectException(url);
    }

    /**
     * Created by gcampagn on 4/30/15.
     */
    public abstract static class Object {
        private final String url;

        protected Object(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }

        public abstract String toHumanString();

        public abstract String getType();

        public void resolve() throws UnknownObjectException {
            // nothing to do
        }

        @Override
        public boolean equals(java.lang.Object object) {
            if (object == null)
                return false;
            if (object == this)
                return true;
            if (!(object instanceof Object))
                return false;
            return url.equals(((Object) object).getUrl());
        }

        @Override
        public int hashCode() {
            return url.hashCode();
        }
    }
}
