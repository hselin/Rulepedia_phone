package edu.stanford.braincat.rulepedia.channels.omlet;

import android.content.Context;
import android.content.Intent;

import java.lang.ref.WeakReference;

import edu.stanford.braincat.rulepedia.channels.ServiceBinder;
import edu.stanford.braincat.rulepedia.events.EventSourceHandler;
import edu.stanford.braincat.rulepedia.model.Channel;
import mobisocial.osm.IOsmService;

/**
 * Created by gcampagn on 5/14/15.
 */
public class OmletChannel extends Channel {
    public static final String OMLET_PACKAGE = "mobisocial.omlet";
    public static final String CONTENT_URI = "content://mobisocial.osm/objects";
    public static final String FEED_CONTENT_URI = "content://mobisocial.osm/feeds/";

    private WeakReference<OmletMessageEventSource> sourceRef;
    private final ServiceBinder binder;

    public OmletChannel(OmletChannelFactory factory, String url) {
        super(factory, url);
        Intent intent = new Intent("mobisocial.intent.action.BIND_SERVICE");
        intent.setPackage("mobisocial.omlet");
        binder = new ServiceBinder(intent);
    }

    @Override
    public void enable(Context ctx, EventSourceHandler handler) {
        binder.enable(ctx, handler);
    }

    @Override
    public void disable(Context ctx) {
        binder.disable(ctx);
    }

    @Override
    public String toHumanString() {
        return "Omlet";
    }

    public IOsmService getService() {
        return (IOsmService) binder.getService();
    }

    public OmletMessageEventSource getEventSource() {
        OmletMessageEventSource source;

        if (sourceRef != null)
            source = sourceRef.get();
        else
            source = null;

        if (source == null) {
            source = new OmletMessageEventSource();
            sourceRef = new WeakReference<>(source);
        }

        return source;
    }
}
