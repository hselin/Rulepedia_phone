package edu.stanford.braincat.rulepedia.channels.generic;

import android.util.ArrayMap;
import android.util.Log;

import org.json.JSONException;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;

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
    private static final String LOG_TAG = "rulepedia.Channel.";

    private final String text;
    private final Context ctx;
    private final Scriptable global;

    private final Map<String, WeakReference<EventSource>> eventSourceRefs;

    public GenericChannel(GenericChannelFactory factory, String url, final String id, String text) {
        super(factory, url);
        this.text = text;
        eventSourceRefs = new HashMap<>();
        ctx = Context.enter();
        ctx.setLanguageVersion(Context.VERSION_1_8);
        ctx.setOptimizationLevel(-1);
        global = ctx.initSafeStandardObjects();

        ScriptableObject.putProperty(global, "log", new BaseFunction() {
            @Override
            public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object... args) {
                if (args.length < 1 || args[0] == null)
                    throw new IllegalArgumentException("Must provide at least one argument");

                Log.i(LOG_TAG + id, args[0].toString());

                return Undefined.instance;
            }

            @Override
            public int getArity() {
                return 1;
            }
        });
        
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
