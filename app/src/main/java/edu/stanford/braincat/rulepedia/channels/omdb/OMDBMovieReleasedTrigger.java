package edu.stanford.braincat.rulepedia.channels.omdb;

import edu.stanford.braincat.rulepedia.channels.PollingTrigger;
import edu.stanford.braincat.rulepedia.channels.RefreshingPollingTrigger;
import edu.stanford.braincat.rulepedia.exceptions.RuleExecutionException;

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
    public boolean isFiring() throws RuleExecutionException {
        return getObject().isReleased();
    }
}
