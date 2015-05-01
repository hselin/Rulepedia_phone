package edu.stanford.braincat.rulepedia.channels;

import org.json.JSONException;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.CharBuffer;

/**
 * Created by gcampagn on 5/1/15.
 */
public class Util {
    public static JSONTokener readJSON(InputStream input) throws IOException, JSONException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        StringBuilder builder = new StringBuilder();
        while (true) {
            CharBuffer buffer = CharBuffer.allocate(4096);
            try {
                int read = reader.read(buffer);
                if (read < 0)
                    break;
            } catch (EOFException e) {
                break;
            }

            builder.append(buffer);
        }

        return new JSONTokener(builder.toString());
    }
}
