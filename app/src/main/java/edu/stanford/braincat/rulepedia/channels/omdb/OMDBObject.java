package edu.stanford.braincat.rulepedia.channels.omdb;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import edu.stanford.braincat.rulepedia.channels.HTTPHelper;
import edu.stanford.braincat.rulepedia.channels.RefreshableObject;
import edu.stanford.braincat.rulepedia.exceptions.RuleExecutionException;
import edu.stanford.braincat.rulepedia.exceptions.TriggerValueTypeException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownChannelException;
import edu.stanford.braincat.rulepedia.model.Action;
import edu.stanford.braincat.rulepedia.model.ObjectPool;
import edu.stanford.braincat.rulepedia.model.Trigger;
import edu.stanford.braincat.rulepedia.model.Value;

/**
 * Created by gcampagn on 5/1/15.
 */
public class OMDBObject extends ObjectPool.Object implements RefreshableObject {
    private boolean hasData;
    private boolean released;
    private String title;

    public OMDBObject(String url) {
        super(url);
        this.title = null;
    }

    @Override
    public String toHumanString() {
        if (title != null)
            return "the movie \"" + title + "\"";
        else
            return "a movie";
    }

    @Override
    public String getType() {
        return OMDBObjectFactory.ID;
    }

    @Override
    public Class<? extends Value> getParamType(String method, String name) throws TriggerValueTypeException {
        throw new TriggerValueTypeException("this trigger has no parameters");
    }

    @Override
    public Trigger createTrigger(String method, Map<String, Value> params) throws UnknownChannelException {
        switch (method) {
            case OMDBObjectFactory.MOVIE_RELEASED:
                return new OMDBMovieReleasedTrigger(this);

            default:
                throw new UnknownChannelException(method);
        }
    }

    @Override
    public Action createAction(String method, Map<String, Value> params) throws UnknownChannelException {
        throw new UnknownChannelException(method);
    }

    private void checkData() throws RuleExecutionException {
        if (!hasData)
            throw new RuleExecutionException("Movie data not available");
    }

    public synchronized String getTitle() throws RuleExecutionException {
        checkData();
        return title;
    }

    public synchronized boolean isReleased() throws RuleExecutionException {
        checkData();
        return released;
    }

    private static Date parseReleaseDate(String dateString) throws ParseException {
        DateFormat df = new SimpleDateFormat("dd MMM yyyy");
        return df.parse(dateString);
    }

    @Override
    public synchronized void refresh() throws IOException {
        try {
            JSONTokener jt = HTTPHelper.getJSON(getUrl());
            JSONObject jsonMovie = (JSONObject) jt.nextValue();

            title = jsonMovie.getString("Title");
            released = parseReleaseDate(jsonMovie.getString("Released")).before(new Date());
            hasData = true;
        } catch(ParseException|JSONException e) {
            throw new IOException("Failed to parse web service response", e);
        }
    }
}
