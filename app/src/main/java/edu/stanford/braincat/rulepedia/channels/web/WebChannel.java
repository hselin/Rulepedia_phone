package edu.stanford.braincat.rulepedia.channels.web;

import org.json.JSONException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import edu.stanford.braincat.rulepedia.channels.HTTPHelper;
import edu.stanford.braincat.rulepedia.channels.RefreshableObject;
import edu.stanford.braincat.rulepedia.channels.ScriptableChannel;
import edu.stanford.braincat.rulepedia.events.EventSource;
import edu.stanford.braincat.rulepedia.exceptions.RuleExecutionException;

/**
 * Created by gcampagn on 5/8/15.
 */
public class WebChannel extends ScriptableChannel implements RefreshableObject {
    private final String text;
    private final String id;

    private final Map<String, EventSource> eventSources;

    private String response;


    public WebChannel(WebChannelFactory factory, String url, String id, String text) {
        super(factory, url);
        this.id = id;
        this.text = text;

        this.eventSources = new HashMap<>();

        // FIXME auth
    }

    public EventSource getEventSource(String id) throws JSONException {
        EventSource source = eventSources.get(id);
        if (source != null)
            return source;

        source = ((WebChannelFactory)getFactory()).createEventSource(id);
        eventSources.put(id, source);
        return source;
    }

    @Override
    public synchronized void refresh() throws IOException {
        response = HTTPHelper.getString(getUrl());
    }

    private void checkData() throws RuleExecutionException {
        if (response == null)
            throw new RuleExecutionException("Movie data not available");
    }

    public synchronized String getData() throws RuleExecutionException {
        checkData();
        return response;
    }

    @Override
    public String toHumanString() {
        return text;
    }

}
