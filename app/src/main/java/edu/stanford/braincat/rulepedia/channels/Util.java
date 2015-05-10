package edu.stanford.braincat.rulepedia.channels;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Created by gcampagn on 5/1/15.
 */
public class Util {
    public static String readString(InputStream input) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        StringBuilder builder = new StringBuilder();
        try {
            char[] buffer = new char[2048];
            while (true) {
                int read = reader.read(buffer);
                if (read < 0)
                    break;
                builder.append(buffer);
            }
        } catch (EOFException e) {
        }

        return builder.toString();
    }

    public static JSONTokener readJSON(InputStream input) throws IOException, JSONException {
        return new JSONTokener(readString(input));
    }

    public static void writeJSON(OutputStream output, JSONArray array) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output));
        writer.write(array.toString());
        writer.flush();
    }
}
