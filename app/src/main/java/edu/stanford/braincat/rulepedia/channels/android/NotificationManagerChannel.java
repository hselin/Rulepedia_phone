package edu.stanford.braincat.rulepedia.channels.android;

import edu.stanford.braincat.rulepedia.model.Channel;

/**
 * Created by gcampagn on 5/9/15.
 */
public class NotificationManagerChannel extends Channel {
    public NotificationManagerChannel(NotificationManagerChannelFactory factory, String url) {
        super(factory, url);
    }

    @Override
    public String toHumanString() {
        return "notifications";
    }
}
