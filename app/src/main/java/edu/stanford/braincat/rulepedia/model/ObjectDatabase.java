package edu.stanford.braincat.rulepedia.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by gcampagn on 4/30/15.
 */
public class ObjectDatabase {
    private final Map<String, Object> knownObjects;
    private final ArrayList<ObjectFactory> knownFactories;

    public ObjectDatabase() {
        knownObjects = new HashMap<>();
        knownFactories = new ArrayList<>();
    }

    public synchronized void registerFactory(ObjectFactory factory) {
        knownFactories.add(factory);
    }

    public synchronized Object getObject(String url) {
        Object existing = knownObjects.get(url);
        if (existing != null)
            return existing;

        for (ObjectFactory factory : knownFactories) {
            if (factory.acceptsURL(url))
                return factory.create(url);
        }
    }

    /**
     * Created by gcampagn on 4/30/15.
     */
    public static class Object {
    }
}
