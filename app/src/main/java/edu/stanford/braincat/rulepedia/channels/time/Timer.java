package edu.stanford.braincat.rulepedia.channels.time;

import edu.stanford.braincat.rulepedia.model.Channel;

/**
 * Created by gcampagn on 5/9/15.
 */
public class Timer extends Channel {
    public Timer(TimerFactory factory, String url) {
        super(factory, url);
    }

    @Override
    public String toHumanString() {
        return "a timer";
    }
}
