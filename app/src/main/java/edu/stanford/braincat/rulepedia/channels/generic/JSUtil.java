package edu.stanford.braincat.rulepedia.channels.generic;

import android.util.ArrayMap;

import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.ScriptableObject;

import java.util.Map;

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
        if (object instanceof String)
            return new Value.Text((String)object);
        else if (object instanceof Boolean)
            return new Value.Text(object.toString());
        else if (object instanceof Number)
            return new Value.Number((Number)object);
        else
            return new Value.Text(object.toString());
    }

    public static Object valueToJavascript(Value value) {
        if (value instanceof Value.Text) {
            return ((Value.Text) value).getText();
        } else if (value instanceof Value.Number) {
            return ((Value.Number) value).getNumber();
        } else if (value instanceof Value.DirectObject) {
            return ((Value.DirectObject) value).getObject().getUrl();
        } else if (value instanceof Value.Picture) {
            return value.toString();
        } else {
            // what else?
            return value.toString();
        }
    }
}
