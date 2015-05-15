package edu.stanford.braincat.rulepedia.channels.generic;

import edu.stanford.braincat.rulepedia.model.Value;

/**
 * Created by gcampagn on 5/15/15.
 */
public class JSUtil {
    public Object valueToJavascript(Value value) {
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
