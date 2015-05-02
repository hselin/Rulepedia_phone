package edu.stanford.braincat.rulepedia.channels.omdb;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.stanford.braincat.rulepedia.channels.HTTPHelper;
import edu.stanford.braincat.rulepedia.channels.RefreshableObject;
import edu.stanford.braincat.rulepedia.exceptions.RuleExecutionException;

/**
 * Created by gcampagn on 5/1/15.
 */
public class OMDBObject extends RefreshableObject {
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

    private void checkData() throws RuleExecutionException {
        if (!hasData)
            throw new RuleExecutionException("Movie data not available");
    }

    public String getTitle() throws RuleExecutionException {
        checkData();
        return title;
    }

    public boolean isReleased() throws RuleExecutionException {
        checkData();
        return released;
    }

    private static Date parseReleaseDate(String dateString) throws ParseException {
        DateFormat df = new SimpleDateFormat("dd MMM yyyy");
        return df.parse(dateString);
    }

    @Override
    public void refresh() throws IOException {
        try {
            JSONTokener jt = HTTPHelper.getJSON(getUrl());
            JSONObject jsonMovie = (JSONObject) jt.nextValue();

            title = jsonMovie.getString("Title");
            released = parseReleaseDate(jsonMovie.getString("Released")).before(new Date());
            hasData = true;
        } catch(ParseException pe) {
            throw new IOException("Failed to parse web service response", pe);
        } catch(JSONException je) {
            throw new IOException("Failed to parse web service response", je);
        }
    }
}
