package edu.stanford.braincat.rulepedia.channels;

import android.support.annotation.NonNull;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.DataFormatException;

/**
 * Created by gcampagn on 5/1/15.
 */
public class Util {
    public static class WeakMap<K, V> implements Map<K, V> {
        private final Map<K, WeakReference<V>> impl;

        public WeakMap() {
            impl = new HashMap<>();
        }

        @Override
        public void clear() {
            impl.clear();
        }

        @Override
        public boolean containsKey(Object key) {
            return impl.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return impl.containsValue(new WeakReference<>(value));
        }

        @NonNull
        @Override
        public Set<Entry<K, V>> entrySet() {
            Set<Entry<K, V>> set = new HashSet<>();
            for (Entry<K, WeakReference<V>> e : impl.entrySet()) {
                V strong = e.getValue().get();
                if (strong != null)
                    set.add(new AbstractMap.SimpleEntry<K, V>(e.getKey(), strong));
            }

            return set;
        }

        @Override
        public V get(Object key) {
            WeakReference<V> weak = impl.get(key);
            if (weak == null)
                return null;
            return weak.get();
        }

        @Override
        public boolean isEmpty() {
            return impl.isEmpty();
        }

        @NonNull
        @Override
        public Set<K> keySet() {
            return impl.keySet();
        }

        @Override
        public V put(K key, V value) {
            impl.put(key, new WeakReference<V>(value));
            return value;
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> map) {
            for (Map.Entry<? extends K, ? extends V> e : map.entrySet()) {
                put(e.getKey(), e.getValue());
            }
        }

        @Override
        public V remove(Object key) {
            WeakReference<V> weak = impl.remove(key);
            return weak.get();
        }

        @Override
        public int size() {
            return impl.size();
        }

        @NonNull
        @Override
        public Collection<V> values() {
            Set<V> set = new HashSet<>();
            for (WeakReference<V> weak : impl.values()) {
                V strong = weak.get();
                if (strong != null)
                    set.add(strong);
            }

            return set;
        }
    }

    final private static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHexString(byte[] bytes, int off, int end) {
        char[] hexChars = new char[(end - off) * 2];
        for ( int j = off; j < end; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[(j - off) * 2] = HEX_ARRAY[v >>> 4];
            hexChars[(j - off) * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String readString(InputStream input) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        StringBuilder builder = new StringBuilder();
        try {
            char[] buffer = new char[2048];
            while (true) {
                int read = reader.read(buffer);
                if (read < 0)
                    break;
                builder.append(buffer, 0, read);
            }
        } catch (EOFException ignored) {
        }

        return builder.toString();
    }

    public static JSONTokener readJSON(InputStream input) throws IOException {
        return new JSONTokener(readString(input));
    }

    public static void writeString(OutputStream output, String data) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output));
        writer.write(data);
        writer.flush();
    }

    public static void writeJSON(OutputStream output, JSONArray array) throws IOException {
        writeString(output, array.toString());
    }

    public static String toSHA1(String string) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            // FIXME
            return new String(md.digest(string.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String parseEncodedRule(String encoded) throws DataFormatException, UnsupportedEncodingException {
        /*ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        InflaterOutputStream stream = new InflaterOutputStream(byteStream);

        byte[] bytes = Base64.decode(encoded, Base64.URL_SAFE);
        stream.write(bytes);
        stream.flush();
        stream.close();

        return (JSONObject) new JSONTokener(byteStream.toString("UTF-8")).nextValue();*/
        return new String(Base64.decode(encoded, Base64.URL_SAFE));
    }
}
