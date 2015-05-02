package edu.stanford.braincat.rulepedia.model;

import java.util.HashMap;
import java.util.Map;

import edu.stanford.braincat.rulepedia.channels.omdb.OMDBTriggerFactory;
import edu.stanford.braincat.rulepedia.channels.sms.SMSActionFactory;
import edu.stanford.braincat.rulepedia.channels.sms.SMSTriggerFactory;
import edu.stanford.braincat.rulepedia.exceptions.UnknownChannelException;

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

    public ChannelFactory<C> getChannelFactory(String id) throws UnknownChannelException {
        ChannelFactory<C> factory = knownFactories.get(id);

        if (factory == null)
            throw new UnknownChannelException(id);

        return factory;
    }

    public static class TriggerDatabase extends ChannelDatabase<Trigger> {
        public void load() {
            registerFactory(new OMDBTriggerFactory());
            registerFactory(new SMSTriggerFactory());
        }
    }

    public static class ActionDatabase extends ChannelDatabase<Action> {
        public void load() {
            registerFactory(new SMSActionFactory());
        }
    }
}
