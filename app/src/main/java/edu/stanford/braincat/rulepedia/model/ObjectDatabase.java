package edu.stanford.braincat.rulepedia.model;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;

/**
 * Created by gcampagn on 5/9/15.
 */
public class ObjectDatabase {
    private static final ObjectDatabase instance = new ObjectDatabase();

    private boolean loaded = false;
    private boolean dirty = false;
    private final Map<String, String> objects;

    private ObjectDatabase() {
        objects = new HashMap<>();
    }

    public static ObjectDatabase get() {
        return instance;
    }

    public Channel resolveChannel(String url) throws UnknownObjectException {
        String resolvedUrl = objects.get(url);
        if (resolvedUrl == null)
            throw new UnknownObjectException(url);
        return ChannelPool.get().getObject(resolvedUrl);
    }

    public Contact resolveContact(String url) throws UnknownObjectException {
        String resolvedUrl = objects.get(url);
        if (resolvedUrl == null)
            throw new UnknownObjectException(url);
        return ContactPool.get().getObject(resolvedUrl);
    }

    public void store(String url, ObjectPool.Object object) {
        objects.put(url, object.getUrl());
    }

    public synchronized void load(Context ctx) {
        if (loaded)
            return;

        // TODO
    }

    public synchronized void save(Context ctx) {
        if (!dirty)
            return;

        // TODO
    }
}
