package edu.stanford.braincat.rulepedia.channels.omdb;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

import edu.stanford.braincat.rulepedia.channels.PollingTrigger;
import edu.stanford.braincat.rulepedia.exceptions.RuleExecutionException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;
import edu.stanford.braincat.rulepedia.model.Channel;
import edu.stanford.braincat.rulepedia.model.Trigger;
import edu.stanford.braincat.rulepedia.model.Value;

/**
 * Created by gcampagn on 5/1/15.
 */
public class OMDBMovieReleasedTrigger extends PollingTrigger {
    private volatile Channel channel;

    public OMDBMovieReleasedTrigger(Channel channel) {
        super(PollingTrigger.ONE_DAY);
        this.channel = channel;
    }

    public Channel getChannel() {
        return channel;
    }

    @Override
    public String toHumanString() {
        return channel.toHumanString() + " is released";
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(Trigger.OBJECT, channel.getUrl());
        json.put(Trigger.TRIGGER, OMDBChannelFactory.MOVIE_RELEASED);
        json.put(Trigger.PARAMS, new JSONArray());
        return json;
    }

    @Override
    public void resolve() throws UnknownObjectException {
        Channel newChannel = channel.resolve();
        if (!(newChannel instanceof OMDBChannel))
            throw new UnknownObjectException(newChannel.getUrl());
        channel = newChannel;
    }

    @Override
    public void typeCheck(Map<String, Class<? extends Value>> context) {
        context.put(OMDBChannelFactory.MOVIE_TITLE, Value.Text.class);
    }

    @Override
    public void updateContext(Map<String, Value> context) throws RuleExecutionException {
        context.put(OMDBChannelFactory.MOVIE_TITLE, new Value.Text(((OMDBChannel)channel).getTitle(), true));
    }

    @Override
    public void update() throws RuleExecutionException {
        try {
            ((OMDBChannel) channel).refresh();
        } catch(IOException ioe) {
            throw new RuleExecutionException("IO exception while refreshing object", ioe);
        }
    }

    @Override
    public boolean isFiring() throws RuleExecutionException {
        try {
            return ((OMDBChannel) channel).isReleased();
        } catch(ClassCastException e) {
            throw new RuleExecutionException(e);
        }
    }
}
