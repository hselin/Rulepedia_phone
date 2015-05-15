package edu.stanford.braincat.rulepedia.channels.omlet;

import edu.stanford.braincat.rulepedia.model.Channel;

/**
 * Created by gcampagn on 5/14/15.
 */
public class OmletChannel extends Channel {
    public static final String OMLET_PACKAGE = "mobisocial.omlet";

    public OmletChannel(OmletChannelFactory factory, String url) {
        super(factory, url);
    }

    @Override
    public String toHumanString() {
        return "Omlet";
    }
}
