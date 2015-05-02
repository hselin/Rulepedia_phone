package edu.stanford.braincat.rulepedia.channels.omdb;

import java.util.Map;

import edu.stanford.braincat.rulepedia.exceptions.TriggerValueTypeException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownChannelException;
import edu.stanford.braincat.rulepedia.model.ChannelFactory;
import edu.stanford.braincat.rulepedia.model.ObjectPool;
import edu.stanford.braincat.rulepedia.model.Trigger;
import edu.stanford.braincat.rulepedia.model.Value;

/**
 * Created by gcampagn on 5/1/15.
 */
public class OMDBTriggerFactory extends ChannelFactory<Trigger> {
    @Override
    public String getName() {
        return OMDBObjectFactory.ID;
    }

    @Override
    public Class<? extends Value> getParamType(String method, String name) throws TriggerValueTypeException {
        throw new TriggerValueTypeException("this trigger has no parameters");
    }

    @Override
    public Trigger createChannel(String method, ObjectPool.Object object, Map<String, Value> params) throws UnknownChannelException {
        OMDBObject omdbObject = (OMDBObject)object;

        switch (method) {
            case "movie-released":
                return new OMDBMovieReleasedTrigger(omdbObject);

            default:
                throw new UnknownChannelException(method);
        }
    }
}
