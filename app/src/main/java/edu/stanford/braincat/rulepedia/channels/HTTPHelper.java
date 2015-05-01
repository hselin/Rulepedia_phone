package edu.stanford.braincat.rulepedia.channels;

import org.json.JSONException;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by gcampagn on 5/1/15.
 */
public class HTTPHelper {
    public static JSONTokener getJSON(String stringUrl) throws IOException, JSONException {
        try {
            URL url = new URL(stringUrl);
            InputStream in = null;
            try {
                URLConnection conn = url.openConnection();
                in = conn.getInputStream();

                return Util.readJSON(in);
            } finally {
                if (in != null)
                    in.close();
            }
        } catch(MalformedURLException mue) {
            // this should never happen, it's checked when we create the object
            throw new RuntimeException(mue);
        }
    }
}
