package edu.stanford.braincat.rulepedia.channels.omdb;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import edu.stanford.braincat.rulepedia.channels.HTTPUtil;
import edu.stanford.braincat.rulepedia.channels.RefreshableObject;
import edu.stanford.braincat.rulepedia.exceptions.RuleExecutionException;
import edu.stanford.braincat.rulepedia.model.Channel;

/**
 * Created by gcampagn on 5/1/15.
 */
public class OMDBChannel extends Channel implements RefreshableObject {
    private boolean hasData;
    private boolean released;
    private String title;

    public OMDBChannel(OMDBChannelFactory factory, String url) {
        super(factory, url);
        this.title = null;
    }

    @Override
    public String toHumanString() {
        if (title != null)
            return "the movie \"" + title + "\"";
        else
            return "a movie";
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

    private static Date parseReleaseDate(@NonNull String dateString) throws ParseException {
        DateFormat df = new SimpleDateFormat("dd MMM yyyy", Locale.US);
        return df.parse(dateString);
    }

    @Override
    public synchronized void refresh() throws IOException {
        try {
            JSONTokener jt = HTTPUtil.getJSON(getUrl());
            JSONObject jsonMovie = (JSONObject) jt.nextValue();

            title = jsonMovie.getString("Title");
            released = parseReleaseDate(jsonMovie.getString("Released")).before(new Date());
            hasData = true;
        } catch (ParseException | JSONException e) {
            throw new IOException("Failed to parse web service response", e);
        }
    }
}
