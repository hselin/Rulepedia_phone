package edu.stanford.braincat.rulepedia.channels.omdb;

import edu.stanford.braincat.rulepedia.channels.PollingTrigger;
import edu.stanford.braincat.rulepedia.channels.RefreshingPollingTrigger;
import edu.stanford.braincat.rulepedia.exceptions.RuleExecutionException;
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
    public boolean producesValue(String name, Class<? extends Value> type) {
        switch (name) {
            case OMDBObjectFactory.MOVIE_TITLE:
                return type.equals(Value.Text.class);
            default:
                return false;
        }
    }

    @Override
    public Value getProducedValue(String name) throws RuleExecutionException {
        switch (name) {
            case OMDBObjectFactory.MOVIE_TITLE:
                return new Value.Text(getObject().getTitle());
            default:
                throw new RuntimeException("sms trigger does not produce " + name);
        }
    }

    @Override
    public boolean isFiring() throws RuleExecutionException {
        return getObject().isReleased();
    }
}
