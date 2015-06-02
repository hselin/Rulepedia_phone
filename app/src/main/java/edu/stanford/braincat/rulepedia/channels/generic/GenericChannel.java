package edu.stanford.braincat.rulepedia.channels.generic;

import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.ArrayMap;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeJSON;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import edu.stanford.braincat.rulepedia.events.EventSource;
import edu.stanford.braincat.rulepedia.events.EventSourceHandler;
import edu.stanford.braincat.rulepedia.exceptions.TriggerValueTypeException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownChannelException;
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
    private final ScriptableObject self;

    private final Map<String, GenericChannelPlugin> plugins;
    private final Map<String, WeakReference<EventSource>> eventSourceRefs;

    public GenericChannel(GenericChannelFactory factory, String url, final String id, String text, JSONArray jsonServices)
            throws JSONException, UnknownChannelException {
        super(factory, url);
        this.text = text;
        eventSourceRefs = new HashMap<>();

        plugins = new ArrayMap<>();
        for (int i = 0; i < jsonServices.length(); i++) {
            String serviceType = jsonServices.getString(i);
            plugins.put(serviceType, GenericChannelFactory.createChannelPlugin(id));
        }

        ctx = Context.enter();
        ctx.setLanguageVersion(Context.VERSION_1_8);
        ctx.setOptimizationLevel(-1);
        global = ctx.initSafeStandardObjects();
        self = new NativeObject();
        ScriptableObject.putProperty(global, "self", self);

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

    @Override
    public void enable(android.content.Context ctx, EventSourceHandler handler) throws IOException {
        for (GenericChannelPlugin p : plugins.values()) {
            p.enable(ctx, handler);
            p.update(ctx, self);
        }
    }

    @Override
    public void disable(android.content.Context ctx) throws IOException {
        for (GenericChannelPlugin p : plugins.values())
            p.disable(ctx);
    }

    public Function compileFunction(String body) {
        return ctx.compileFunction(global, body, "channels.json", 1, null);
    }

    public Object callFunction(Function function, Scriptable thisArg, Object... args) {
        return function.call(ctx, global, thisArg, args);
    }

    public String toJSON(Object value) {
        return NativeJSON.stringify(ctx, global, value, null, 0).toString();
    }

    public Object fromJSON(String json) {
        return NativeJSON.parse(ctx, global, json, new BaseFunction() {
            @Override
            public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object... args) {
                if (args.length != 2)
                    throw new IllegalArgumentException("Invalid argument to reviver function");

                return args[1];
            }

            @Override
            public int getArity() {
                return 2;
            }
        });
    }

    @Nullable
    public IBinder getService(String serviceType) {
        GenericChannelPlugin plugin = plugins.get(serviceType);
        if (plugin == null)
            return null;
        return plugin.getService();
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
