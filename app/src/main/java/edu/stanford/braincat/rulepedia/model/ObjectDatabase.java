package edu.stanford.braincat.rulepedia.model;

import android.content.Context;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import edu.stanford.braincat.rulepedia.exceptions.UnexpectedPlaceholderException;
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

    private String resolveUrl(String url) throws UnknownObjectException {
        String resolvedUrl = objects.get(url);
        if (resolvedUrl == null)
            throw new UnexpectedPlaceholderException(url);
        return resolvedUrl;
    }

    public <K extends ObjectPool.Object<K, ?>> K resolve(ObjectPool<K, ?> pool, ObjectPool.Object<K, ?> placeholder) throws UnknownObjectException {
        return pool.getObject(resolveUrl(placeholder.getUrl()));
    }

    public synchronized Collection<Property> getAllProperties() {
        ArrayList<Property> properties = new ArrayList<>();

        for (Map.Entry<String, String> pair : objects.entrySet()) {
            properties.add(new Property(pair.getKey(), pair.getValue()));
        }

        return properties;
    }

    private <K extends ObjectPool.Object<K, ?>> void storeUrl(String placeholderUrl, K object, ObjectPool<K, ?> pool) {
        pool.cache(object);
        objects.put(placeholderUrl, object.getUrl());
        dirty = true;
    }

    public synchronized void store(String url, Contact object) {
        storeUrl(url, object, ContactPool.get());
    }

    public synchronized void store(String url, Channel object) {
        storeUrl(url, object, ChannelPool.get());
    }

    public synchronized void store(String url, Device object) {
        storeUrl(url, object, DevicePool.get());
    }

    public synchronized void remove(String url) {
        objects.remove(url);
        dirty = true;
    }

    public synchronized void load(Context ctx) throws IOException {
        if (loaded)
            return;
        loaded = true;

        try (FileInputStream file = ctx.openFileInput("objects.db")) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(file));

            try {
                while (true) {
                    String line = reader.readLine();
                    if (line == null)
                        break;

                    String[] split = line.split(" ");
                    if (split.length != 2)
                        throw new IOException("Invalid object database format on disk");

                    objects.put(split[0], split[1]);
                }
            } catch (EOFException e) {
                // done
            }
        } catch (FileNotFoundException e) {
            // ignore
        }
    }

    public synchronized void save(Context ctx) throws IOException {
        if (!dirty)
            return;
        dirty = false;

        try (FileOutputStream file = ctx.openFileOutput("objects.db", Context.MODE_PRIVATE)) {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(file));

            for (Map.Entry<String, String> entry : objects.entrySet()) {
                writer.write(entry.getKey());
                writer.write(' ');
                writer.write(entry.getValue());
            }

            writer.flush();
        }
    }
}
