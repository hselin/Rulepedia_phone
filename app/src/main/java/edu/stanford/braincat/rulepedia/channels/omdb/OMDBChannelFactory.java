package edu.stanford.braincat.rulepedia.channels.omdb;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.braincat.rulepedia.exceptions.TriggerValueTypeException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownChannelException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;
import edu.stanford.braincat.rulepedia.model.Action;
import edu.stanford.braincat.rulepedia.model.Channel;
import edu.stanford.braincat.rulepedia.model.ChannelFactory;
import edu.stanford.braincat.rulepedia.model.Trigger;
import edu.stanford.braincat.rulepedia.model.Value;

/**
 * Created by gcampagn on 5/1/15.
 */
public class OMDBChannelFactory extends ChannelFactory {
    private final Pattern ombdByIdPattern;

    public static final String ID = "omdb";
    public static final String MOVIE_RELEASED = "movie-released";
    public static final String MOVIE_TITLE = "movie-title";

    public OMDBChannelFactory() {
        super("http://www.omdbapi.com/?r=json&v=1&i=");

        ombdByIdPattern = Pattern.compile("^http://www\\.omdbapi\\.com/\\?r=json&v=1&i=[[a-z][0-9]]+$");
    }

    @Override
    public Channel create(String url) throws UnknownObjectException {
        Matcher m;

        m = ombdByIdPattern.matcher(url);
        if (m.matches())
            return new OMDBChannel(this, url);

        throw new UnknownObjectException(url);
    }

    @Override
    public Channel createPlaceholder(String url) {
        return new Channel(this, url) {
            @Override
            public String toHumanString() {
                return "a movie";
            }

        };
    }

    @Override
    public String getName() {
        return ID;
    }

    @Override
    public Class<? extends Value> getParamType(String method, String name) throws TriggerValueTypeException {
        throw new TriggerValueTypeException("this trigger has no parameters");
    }

    @Override
    public Trigger createTrigger(Channel channel, String method, Map<String, Value> params) throws UnknownChannelException {
        switch (method) {
            case OMDBChannelFactory.MOVIE_RELEASED:
                return new OMDBMovieReleasedTrigger(channel);

            default:
                throw new UnknownChannelException(method);
        }
    }

    @Override
    public Action createAction(Channel channel, String method, Map<String, Value> params) throws UnknownChannelException {
        throw new UnknownChannelException(method);
    }
}
