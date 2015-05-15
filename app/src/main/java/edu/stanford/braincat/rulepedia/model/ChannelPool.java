package edu.stanford.braincat.rulepedia.model;

import edu.stanford.braincat.rulepedia.channels.android.NotificationManagerChannelFactory;
import edu.stanford.braincat.rulepedia.channels.android.SMSChannelFactory;
import edu.stanford.braincat.rulepedia.channels.googlefit.GoogleFitChannelFactory;
import edu.stanford.braincat.rulepedia.channels.omdb.OMDBChannelFactory;
import edu.stanford.braincat.rulepedia.channels.omlet.OmletChannelFactory;
import edu.stanford.braincat.rulepedia.channels.time.TimerFactory;

/**
 * Created by gcampagn on 5/9/15.
 */
public class ChannelPool extends ObjectPool<Channel, ChannelFactory> {
    public static final String KIND = "channel";
    public static final String PREDEFINED_PREFIX = ObjectPool.PREDEFINED_PREFIX + KIND + "/";
    public static final String PLACEHOLDER_PREFIX = ObjectPool.PLACEHOLDER_PREFIX + KIND + "/";

    private static final ChannelPool instance = new ChannelPool();

    public static ChannelPool get() {
        return instance;
    }

    public ChannelPool() {
        super(KIND);

        registerFactory(new TimerFactory());
        registerFactory(new SMSChannelFactory());
        registerFactory(new NotificationManagerChannelFactory());
        registerFactory(new OMDBChannelFactory());
        registerFactory(new GoogleFitChannelFactory());
        registerFactory(new OmletChannelFactory());
    }


}
