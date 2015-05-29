package edu.stanford.braincat.rulepedia.channels.googlefit;

import android.content.Context;

import com.google.android.gms.fitness.data.Session;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import edu.stanford.braincat.rulepedia.channels.SingleEventTrigger;
import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;
import edu.stanford.braincat.rulepedia.model.Channel;
import edu.stanford.braincat.rulepedia.model.ObjectPool;
import edu.stanford.braincat.rulepedia.model.Trigger;
import edu.stanford.braincat.rulepedia.model.Value;

/**
 * Created by gcampagn on 5/13/15.
 */
public class EndActivityTrigger extends SingleEventTrigger<ActivityMonitorEventSource> {
    public static final String ACTIVITY_DESCRIPTION = "activity-description";
    public static final String ACTIVITY_DURATION = "activity-duration";
    public static final String ACTIVITY_END_TIME = "activity-end-time";

    private volatile Channel channel;
    private Session activity;

    public EndActivityTrigger(Channel channel) {
        this.channel = channel;
    }

    @Override
    public Channel getChannel() {
        return channel;
    }

    @Override
    public Collection<ObjectPool.Object> getPlaceholders() {
        Collection<ObjectPool.Object> result = new HashSet<>();

        Channel currentChannel = channel;
        if (currentChannel.isPlaceholder())
            result.add(currentChannel);

        return result;
    }

    @Override
    public void resolve() throws UnknownObjectException {
        Channel newChannel = channel.resolve();
        if (!(newChannel instanceof GoogleFitChannel))
            throw new UnknownObjectException(newChannel.getUrl());
        setSource(((GoogleFitChannel) newChannel).getActivityMonitorEventSource());
        channel = newChannel;
    }

    @Override
    public void update(Context ctx) {
        if (!getSource().checkEvent()) {
            activity = null;
            return;
        }

        ActivityMonitorEventSource.Event event = getSource().getLastEvent();

        if (event.getType() == ActivityMonitorEventSource.Event.Type.ACTIVITY_END)
            activity = event.getSession();
        else
            activity = null;
    }

    @Override
    public boolean isFiring() {
        return activity != null;
    }

    @Override
    public String toHumanString() {
        return "I finish a fitness activity";
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(Trigger.OBJECT, channel.getUrl());
        json.put(Trigger.TRIGGER, GoogleFitChannelFactory.END_ACTIVITY);
        json.put(Trigger.PARAMS, new JSONArray());
        return json;
    }

    @Override
    public void typeCheck(Map<String, Class<? extends Value>> context) {
        context.put(ACTIVITY_DESCRIPTION, Value.Text.class);
        context.put(ACTIVITY_DURATION, Value.Number.class);
        context.put(ACTIVITY_END_TIME, Value.Text.class);
    }

    @Override
    public void updateContext(Map<String, Value> context) {
        context.put(ACTIVITY_DESCRIPTION, new Value.Text(activity.getDescription(), true));
        context.put(ACTIVITY_DURATION,
                new Value.Number(activity.getEndTime(TimeUnit.MILLISECONDS) - activity.getStartTime(TimeUnit.MILLISECONDS)));
        context.put(ACTIVITY_END_TIME, new Value.Text(new Date(activity.getEndTime(TimeUnit.MILLISECONDS)).toLocaleString(), true));
    }
}
