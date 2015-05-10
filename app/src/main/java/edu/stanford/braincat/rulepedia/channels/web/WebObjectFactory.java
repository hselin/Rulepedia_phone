package edu.stanford.braincat.rulepedia.channels.web;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;
import edu.stanford.braincat.rulepedia.model.ObjectFactory;
import edu.stanford.braincat.rulepedia.model.ObjectPool;

/**
 * Created by gcampagn on 5/8/15.
 */
public class WebObjectFactory extends ObjectFactory {
    private final JSONObject jsonFactory;
    private final String id;
    private final Pattern pattern;

    public WebObjectFactory(JSONObject jsonObjectFactory) throws JSONException {
        super(jsonObjectFactory.getString("urlPrefix"));
        jsonFactory = jsonObjectFactory;
        id = jsonObjectFactory.getString("id");
        pattern = Pattern.compile(jsonObjectFactory.getString("urlRegex"));
    }

    @Override
    public ObjectPool.Object create(String url) throws UnknownObjectException {
        try {
            return new WebObject(url, jsonFactory);
        } catch(JSONException e) {
            throw new UnknownObjectException(url);
        }
    }

    @Override
    public String getName() {
        return id;
    }
}
