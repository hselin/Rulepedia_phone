package edu.stanford.braincat.rulepedia.model;

import java.util.regex.Pattern;

/**
 * Created by gcampagn on 4/30/15.
 */
public abstract class ObjectFactory {
    private final Pattern pattern;

    protected ObjectFactory(Pattern pattern) {
        this.pattern = pattern;
    }

    public boolean acceptsURL(String url) {
        return pattern.matcher(url).matches();
    }

    public abstract ObjectDatabase.Object create(String url);
}
