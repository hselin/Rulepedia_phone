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

    public synchronized Channel resolveChannel(String url) throws UnknownObjectException {
        String resolvedUrl = objects.get(url);
        if (resolvedUrl == null)
            throw new UnknownObjectException(url);
        return ChannelPool.get().getObject(resolvedUrl);
    }

    public synchronized Collection<Property> getAllProperties() {
        ArrayList<Property> properties = new ArrayList<>();

        for (Map.Entry<String, String> pair : objects.entrySet()) {
            properties.add(new Property(pair.getKey(), pair.getValue()));
        }

        return properties;
    }

    public synchronized Contact resolveContact(String url) throws UnknownObjectException {
        String resolvedUrl = objects.get(url);
        if (resolvedUrl == null)
            throw new UnknownObjectException(url);
        return ContactPool.get().getObject(resolvedUrl);
    }

    public synchronized Device resolveDevice(String url) throws UnknownObjectException {
        String resolvedUrl = objects.get(url);
        if (resolvedUrl == null)
            throw new UnknownObjectException(url);
        return DevicePool.get().getObject(resolvedUrl);
    }

    public synchronized void store(String url, ObjectPool.Object object) {
        objects.put(url, object.getUrl());
        dirty = true;
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
