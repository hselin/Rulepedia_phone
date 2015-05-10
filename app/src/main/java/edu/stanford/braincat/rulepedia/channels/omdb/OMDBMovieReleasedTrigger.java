package edu.stanford.braincat.rulepedia.channels.omdb;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import edu.stanford.braincat.rulepedia.channels.PollingTrigger;
import edu.stanford.braincat.rulepedia.channels.RefreshingPollingTrigger;
import edu.stanford.braincat.rulepedia.exceptions.RuleExecutionException;
import edu.stanford.braincat.rulepedia.model.Trigger;
import edu.stanford.braincat.rulepedia.model.Value;

/**
 * Created by gcampagn on 5/1/15.
 */
public class OMDBMovieReleasedTrigger extends RefreshingPollingTrigger<OMDBObject> {
    public OMDBMovieReleasedTrigger(OMDBObject o) {
        super(o, PollingTrigger.ONE_DAY);
    }

    @Override
    public String toHumanString() {
        return getObject().toHumanString() + " is released";
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(Trigger.OBJECT, getObject().getUrl());
        json.put(Trigger.TRIGGER, OMDBObjectFactory.MOVIE_RELEASED);
        json.put(Trigger.PARAMS, new JSONArray());
        return json;
    }

    @Override
    public void typeCheck(Map<String, Class<? extends Value>> context) {
        context.put(OMDBObjectFactory.MOVIE_TITLE, Value.Text.class);
    }

    @Override
    public void updateContext(Map<String, Value> context) throws RuleExecutionException {
        context.put(OMDBObjectFactory.MOVIE_TITLE, new Value.Text(getObject().getTitle(), true));
    }

    @Override
    public boolean isFiring() throws RuleExecutionException {
        return getObject().isReleased();
    }
}
