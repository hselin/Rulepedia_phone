package edu.stanford.braincat.rulepedia.channels.email;

import edu.stanford.braincat.rulepedia.model.Channel;

/**
 * Created by gcampagn on 5/29/15.
 */
public class EmailChannel extends Channel {
    public EmailChannel(EmailChannelFactory factory, String url) {
        super(factory, url);
    }

    @Override
    public String toHumanString() {
        return "emails";
    }
}
