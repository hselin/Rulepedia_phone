package edu.stanford.braincat.rulepedia.channels.omdb;

import java.io.IOException;

import edu.stanford.braincat.rulepedia.channels.RefreshableObject;
import edu.stanford.braincat.rulepedia.exceptions.RuleExecutionException;

/**
 * Created by gcampagn on 5/1/15.
 */
public class OMDBObject extends RefreshableObject {
    private final String id;

    private boolean hasData;
    private boolean released;
    private String title;

    public OMDBObject(String url, String id) {
        super(url);
        this.id = id;
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
    public void resolve() {
        // nothing to do
    }

    private void checkData() throws RuleExecutionException {
        if (!hasData)
            throw new RuleExecutionException("Movie data not available");
    }

    public boolean isReleased() throws RuleExecutionException {
        checkData();
        return released;
    }

    @Override
    public void refresh() throws IOException {
        // TODO
    }
}
