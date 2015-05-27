package edu.stanford.braincat.rulepedia.channels.omlet;

import android.content.Intent;

import java.lang.ref.WeakReference;

import edu.stanford.braincat.rulepedia.channels.BindServiceChannel;

/**
 * Created by gcampagn on 5/14/15.
 */
public class OmletChannel extends BindServiceChannel {
    public static final String OMLET_PACKAGE = "mobisocial.omlet";
    public static final String CONTENT_URI = "content://mobisocial.osm/objects";
    public static final String FEED_CONTENT_URI = "content://mobisocial.osm/feeds/";

    private WeakReference<OmletMessageEventSource> sourceRef;

    public OmletChannel(OmletChannelFactory factory, String url) {
        super(factory, url);
    }

    @Override
    public String toHumanString() {
        return "Omlet";
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

    @Override
    protected Intent createIntent() {
        Intent intent = new Intent("mobisocial.intent.action.BIND_SERVICE");
        intent.setPackage("mobisocial.omlet");
        return intent;
    }
}
