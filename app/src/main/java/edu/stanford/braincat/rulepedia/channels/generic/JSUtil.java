package edu.stanford.braincat.rulepedia.channels.generic;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.ArrayMap;

import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.ScriptableObject;

import java.io.Serializable;
import java.util.Map;

import edu.stanford.braincat.rulepedia.channels.omlet.OmletMessage;
import edu.stanford.braincat.rulepedia.model.Value;

/**
 * Created by gcampagn on 5/15/15.
 */
public class JSUtil {
    public static NativeObject parametersToJavascript(Map<String, Value> params) {
        NativeObject object = new NativeObject();

        for (Map.Entry<String, Value> e : params.entrySet())
            ScriptableObject.putProperty(object, e.getKey(), valueToJavascript(e.getValue()));

        return object;
    }

    public static Map<String, Value> javascriptToParameters(NativeObject object) {
        Map<String, Value> result = new ArrayMap<>();

        for (Map.Entry<Object, Object> e : object.entrySet())
            result.put(e.getKey().toString(), javascriptToValue(e.getValue()));

        return result;
    }

    public static Value javascriptToValue(Object object) {
        if (object == null)
            throw new NullPointerException();
        if (object instanceof String)
            return new Value.Text((String) object);
        else if (object instanceof Boolean)
            return new Value.Text(object.toString());
        else if (object instanceof Number)
            return new Value.Number((Number) object);
        else
            return new Value.Text(object.toString());
    }

    public static Object valueToJavascript(@Nullable Value value) {
        if (value == null) {
            return null;
        } else if (value instanceof Value.Text) {
            return ((Value.Text) value).getText();
        } else if (value instanceof Value.Number) {
            return ((Value.Number) value).getNumber();
        } else if (value instanceof Value.DirectObject) {
            return ((Value.DirectObject) value).getObject().getUrl();
        } else {
            // Contact, Picture, DirectPicture, something else?
            return value.toString();
        }
    }

    public static Intent javascriptToIntent(ScriptableObject object) {
        Intent intent = new Intent((String) ScriptableObject.getProperty(object, "action"));

        if (ScriptableObject.hasProperty(object, "categories")) {
            for (String cat : (String[]) ScriptableObject.getProperty(object, "categories"))
                intent.addCategory(cat);
        }

        if (ScriptableObject.hasProperty(object, "package"))
            intent.setPackage((String) ScriptableObject.getProperty(object, "package"));

        if (ScriptableObject.hasProperty(object, "extras")) {
            ScriptableObject extras = (ScriptableObject) ScriptableObject.getProperty(object, "extras");
            for (Object id : ScriptableObject.getPropertyIds(extras)) {
                Object value = ScriptableObject.getProperty(extras, id.toString());

                if (value == null)
                    continue;
                if (value instanceof Boolean)
                    intent.putExtra(id.toString(), ((Boolean) value).booleanValue());
                else if (value instanceof String)
                    intent.putExtra(id.toString(), (String) value);
                else if (value instanceof Integer)
                    intent.putExtra(id.toString(), ((Integer) value).intValue());
                else if (value instanceof Double)
                    intent.putExtra(id.toString(), ((Double) value).doubleValue());
                else if (value instanceof Serializable)
                    intent.putExtra(id.toString(), (Serializable) value);
                else
                    intent.putExtra(id.toString(), value.toString());
            }
        }

        return intent;
    }

    public static ScriptableObject intentToJavascript(Intent intent) {
        ScriptableObject object = new NativeObject();

        ScriptableObject.putProperty(object, "action", intent.getAction());
        ScriptableObject.putProperty(object, "categories", new NativeArray(intent.getCategories().toArray()));

        ScriptableObject jsExtras = new NativeObject();
        ScriptableObject.putProperty(object, "extras", jsExtras);

        Bundle extras = intent.getExtras();
        if (extras != null) {
            for (String key : extras.keySet())
                ScriptableObject.putProperty(jsExtras, key, extras.get(key));
        }

        return object;
    }

    public static ScriptableObject omletMessageToJavascript(OmletMessage message, GenericChannel channel, Context ctx) {
        String json = message.getJSON(ctx);
        if (json == null)
            return null;

        ScriptableObject object = new NativeObject();

        ScriptableObject.putProperty(object, "feedUri", message.getFeedUri());
        ScriptableObject.putProperty(object, "type", message.getType());
        ScriptableObject.putProperty(object, "message", channel.fromJSON(json));

        return object;
    }
}
