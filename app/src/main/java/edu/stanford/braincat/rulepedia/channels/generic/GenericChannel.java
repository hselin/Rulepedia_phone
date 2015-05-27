package edu.stanford.braincat.rulepedia.channels.generic;

import android.util.ArrayMap;

import org.json.JSONException;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import edu.stanford.braincat.rulepedia.events.EventSource;
import edu.stanford.braincat.rulepedia.exceptions.TriggerValueTypeException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;
import edu.stanford.braincat.rulepedia.model.Channel;

/**
 * Created by gcampagn on 5/8/15.
 */
public class GenericChannel extends Channel {
    private final String text;
    private final Context ctx;
    private final Scriptable global;

    private final Map<String, WeakReference<EventSource>> eventSourceRefs;

    public GenericChannel(GenericChannelFactory factory, String url, String text) {
        super(factory, url);
        this.text = text;
        this.eventSourceRefs = new HashMap<>();
        this.ctx = Context.enter();
        ctx.setLanguageVersion(Context.VERSION_1_8);
        ctx.setOptimizationLevel(-1);
        this.global = ctx.initSafeStandardObjects();
        // FIXME auth
    }

    public Function compileFunction(String body) {
        return ctx.compileFunction(global, body, "channels.json", 1, null);
    }

    public Object callFunction(Function function, Scriptable thisArg, Object... args) {
        return function.call(ctx, global, thisArg, args);
    }

    public Map<String, EventSource> getEventSources() throws
            MalformedURLException, JSONException, TriggerValueTypeException, UnknownObjectException {
        Map<String, EventSource> result = new ArrayMap<>();

        Collection<String> names = ((GenericChannelFactory) getFactory()).getEventSourceNames();

        for (String name : names) {
            WeakReference<EventSource> sourceRef = eventSourceRefs.get(name);
            EventSource source = sourceRef.get();

            if (source == null) {
                source = ((GenericChannelFactory) getFactory()).createEventSource(this, name);
                eventSourceRefs.put(name, new WeakReference<>(source));
            }

            result.put(name, source);
        }

        return result;
    }

    @Override
    public String toHumanString() {
        return text;
    }

}
