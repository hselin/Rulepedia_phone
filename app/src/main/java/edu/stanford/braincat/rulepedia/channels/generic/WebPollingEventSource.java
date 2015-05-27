package edu.stanford.braincat.rulepedia.channels.generic;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Queue;

import edu.stanford.braincat.rulepedia.events.EventSource;
import edu.stanford.braincat.rulepedia.events.EventSourceHandler;
import edu.stanford.braincat.rulepedia.events.TimeoutEventSource;

/**
 * Created by gcampagn on 5/15/15.
 */
public class WebPollingEventSource implements EventSource {
    private final URL url;
    private final EventSource pollingSource;
    private final Queue<InputStream> requestQueue;

    public WebPollingEventSource(String url, long timeout) throws MalformedURLException {
        this.url = new URL(url);
        this.pollingSource = new TimeoutEventSource(timeout);
        this.requestQueue = new ArrayDeque<>();
    }

    @Override
    public void install(Context ctx, EventSourceHandler handler) throws IOException {
        pollingSource.install(ctx, handler);
    }

    @Override
    public void uninstall(Context ctx) throws IOException {
        pollingSource.uninstall(ctx);
    }

    public InputStream getLastConnection() {
        return requestQueue.element();
    }

    @Override
    public boolean checkEvent() throws IOException {
        if (!requestQueue.isEmpty())
            return true;

        if (pollingSource.checkEvent()) {
            requestQueue.offer(new BufferedInputStream(url.openStream()));
            return true;
        }

        return false;
    }

    @Override
    public void updateState() throws IOException {
        requestQueue.poll().close();
    }
}
