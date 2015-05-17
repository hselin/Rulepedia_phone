package edu.stanford.braincat.rulepedia.channels;

import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by gcampagn on 5/1/15.
 */
public class HTTPUtil {
    public static String getString(String stringUrl) throws IOException {
        try {
            URL url = new URL(stringUrl);
            try (InputStream in = url.openStream()) {
                return Util.readString(in);
            }
        } catch (MalformedURLException mue) {
            // this should never happen, it's checked when we create the object
            throw new RuntimeException(mue);
        }
    }

    public static JSONTokener getJSON(String stringUrl) throws IOException {
        return new JSONTokener(getString(stringUrl));
    }
}
