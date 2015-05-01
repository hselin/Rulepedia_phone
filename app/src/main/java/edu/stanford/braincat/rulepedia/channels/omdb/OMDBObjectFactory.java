package edu.stanford.braincat.rulepedia.channels.omdb;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;
import edu.stanford.braincat.rulepedia.model.ObjectFactory;
import edu.stanford.braincat.rulepedia.model.ObjectPool;

/**
 * Created by gcampagn on 5/1/15.
 */
public class OMDBObjectFactory extends ObjectFactory {
    private final Pattern ombdByIdPattern;

    public static final String ID = "omdb";

    public OMDBObjectFactory() {
        super("http://www.omdbapi.com/?r=json&v=1&i=");

        ombdByIdPattern = Pattern.compile("^http://www.omdbapi.com/\\?r=json&v=1&i=([a-z0-9]+)$");
    }

    @Override
    public ObjectPool.Object create(String url) throws UnknownObjectException {
        Matcher m;

        m = ombdByIdPattern.matcher(url);
        if (m.matches()) {
            String id = m.group(1);

            return new OMDBObject(url, id);
        }

        throw new UnknownObjectException(url);
    }

    @Override
    public String getName() {
        return ID;
    }
}
