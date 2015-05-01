package edu.stanford.braincat.rulepedia.model;

import java.util.HashMap;
import java.util.Map;

import edu.stanford.braincat.rulepedia.exceptions.UnknownChannelException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;

/**
 * Created by gcampagn on 4/30/15.
 */
public abstract class ChannelDatabase<C> {
    private final Map<String, ChannelFactory<C> > knownFactories;

    public ChannelDatabase() {
        knownFactories = new HashMap<>();
    }

    public abstract void load();

    protected void registerFactory(ChannelFactory<C> factory) {
        knownFactories.put(factory.getName(), factory);
    }

    public C createChannel(String id, ObjectPool.Object on, HashMap<String, Value> params) throws UnknownObjectException, UnknownChannelException {
        ChannelFactory<C> factory = knownFactories.get(id);

        if (factory == null)
            throw new UnknownChannelException(id);

        return factory.createChannel(on, params);
    }

    public static class TriggerDatabase extends ChannelDatabase<Trigger> {
        public void load() {
            // TODO: fill me with instantiation of the real trigger factories
        }
    }

    public static class ActionDatabase extends ChannelDatabase<Action> {
        public void load() {
            // TODO: fill me with instantiation of the real action factories
        }
    }
}
