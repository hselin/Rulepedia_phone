package edu.stanford.braincat.rulepedia.model;

import java.util.Map;

import edu.stanford.braincat.rulepedia.exceptions.UnknownChannelException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;

/**
 * Created by gcampagn on 4/30/15.
 */
public abstract class ChannelFactory<C> {
    public abstract String getName();

    public abstract C createChannel(String method, ObjectPool.Object object, Map<String, Value> params) throws UnknownObjectException, UnknownChannelException;
}
