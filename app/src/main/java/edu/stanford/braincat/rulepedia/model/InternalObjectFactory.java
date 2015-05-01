package edu.stanford.braincat.rulepedia.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;

/**
 * Created by gcampagn on 4/30/15.
 *
 * Handles internal objects (placeholders, trigger values, etc.)
 */
public class InternalObjectFactory extends ObjectFactory {
    private final Pattern placeholderPattern;

    public InternalObjectFactory() {
        super("rulepedia:/");

        placeholderPattern = Pattern.compile("^rulepedia:/placeholder/object/([a-b-]+)$");
    }

    public String getName() {
        return "internal";
    }

    public ObjectPool.Object create(String url) throws UnknownObjectException {
        Matcher m;

        m = placeholderPattern.matcher(url);
        if (m.matches()) {
            String subType = m.group(1);

            return new Placeholder(url, subType);
        }

        throw new UnknownObjectException(url);
    }

    public static class Placeholder extends ObjectPool.Object {
        private final String type;

        private Placeholder(String url, String type) {
            super(url);
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public void resolve() throws UnknownObjectException {
            throw new UnknownObjectException(getUrl());
        }

        public String toHumanString() {
            return "any object of type " + type;
        }
    }
}
