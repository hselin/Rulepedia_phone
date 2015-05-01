package edu.stanford.braincat.rulepedia.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;

/**
 * Created by gcampagn on 4/30/15.
 */
public class ObjectPool {
    private final Map<String, Object> knownObjects;
    private final ArrayList<ObjectFactory> knownFactories;

    public ObjectPool() {
        knownObjects = new HashMap<>();
        knownFactories = new ArrayList<>();
    }

    public synchronized void registerFactory(ObjectFactory factory) {
        knownFactories.add(factory);
    }

    public synchronized Object getObject(String url) throws UnknownObjectException {
        Object existing = knownObjects.get(url);
        if (existing != null)
            return existing;

        for (ObjectFactory factory : knownFactories) {
            if (factory.acceptsURL(url))
                return factory.create(url);
        }

        throw new UnknownObjectException(url);
    }

    /**
     * Created by gcampagn on 4/30/15.
     */
    public static class Object {
    }
}