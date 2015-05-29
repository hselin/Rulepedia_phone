package edu.stanford.braincat.rulepedia.channels;

import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by gcampagn on 5/1/15.
 */
public class HTTPUtil {
    public static String getString(String stringUrl) throws IOException {
        URL url = new URL(stringUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try (InputStream in = connection.getInputStream()) {
            return Util.readString(in);
        } finally {
            connection.disconnect();
        }
    }

    public static JSONTokener getJSON(String stringUrl) throws IOException {
        return new JSONTokener(getString(stringUrl));
    }

    public static String postString(String stringUrl, String data) throws IOException {
         URL url = new URL(stringUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        try {
            if (data != null) {
                connection.setDoOutput(true);
                try (OutputStream out = connection.getOutputStream()) {
                    Util.writeString(out, data);
                }
            }
            try (InputStream in = connection.getInputStream()) {
                return Util.readString(in);
            }
        } finally {
            connection.disconnect();
        }
    }
}
