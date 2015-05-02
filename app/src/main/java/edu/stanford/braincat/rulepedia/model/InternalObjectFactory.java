package edu.stanford.braincat.rulepedia.model;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.braincat.rulepedia.channels.sms.SMSChannel;
import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;

/**
 * Created by gcampagn on 4/30/15.
 *
 * Handles internal objects (placeholders, trigger values, etc.)
 */
public class InternalObjectFactory extends ObjectFactory {
    private static abstract class PredefinedObjectFactory {
        public abstract ObjectPool.Object create();
    }

    public static final String PREDEFINED_PREFIX = "rulepedia:predefined/object/";

    private final HashMap<String, PredefinedObjectFactory> predefinedObjects;
    private final Pattern predefinedPattern;
    private final Pattern placeholderPattern;

    public InternalObjectFactory() {
        super("rulepedia:");

        predefinedPattern = Pattern.compile("^rulepedia:predefined/object/([a-b-]+)$");
        placeholderPattern = Pattern.compile("^rulepedia:placeholder/object/([a-b-]+)$");

        predefinedObjects = new HashMap<>();
        predefinedObjects.put(SMSChannel.ID, new PredefinedObjectFactory() {
            @Override
            public ObjectPool.Object create() {
                return new SMSChannel();
            }
        });
        // FIXME: fill with predefined stuff like weather or phone
    }

    public String getName() {
        return "internal";
    }

    public ObjectPool.Object create(String url) throws UnknownObjectException {
        Matcher m;

        m = predefinedPattern.matcher(url);
        if (m.matches()) {
            String id = m.group(1);
            ObjectPool.Object object = predefinedObjects.get(id).create();
            if (object == null)
                throw new UnknownObjectException(url);

            return object;
        }

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
