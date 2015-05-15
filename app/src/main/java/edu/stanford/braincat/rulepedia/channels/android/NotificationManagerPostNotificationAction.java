package edu.stanford.braincat.rulepedia.channels.android;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import edu.stanford.braincat.rulepedia.R;
import edu.stanford.braincat.rulepedia.exceptions.RuleExecutionException;
import edu.stanford.braincat.rulepedia.exceptions.TriggerValueTypeException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;
import edu.stanford.braincat.rulepedia.model.Action;
import edu.stanford.braincat.rulepedia.model.Channel;
import edu.stanford.braincat.rulepedia.model.ObjectPool;
import edu.stanford.braincat.rulepedia.model.Value;

/**
 * Created by gcampagn on 5/9/15.
 */
public class NotificationManagerPostNotificationAction implements Action {
    private volatile Channel channel;
    private final Value title;
    private final Value text;

    public NotificationManagerPostNotificationAction(Channel channel, Value title, Value text) {
        this.channel = channel;
        this.title = title;
        this.text = text;
    }

    public Channel getChannel() {
        return channel;
    }

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
        if (!(newChannel instanceof NotificationManagerChannel))
            throw new UnknownObjectException(newChannel.getUrl());
        channel = newChannel;
    }

    @Override
    public void typeCheck(Map<String, Class<? extends Value>> context) throws TriggerValueTypeException {
        title.typeCheck(context, Value.Text.class);
        text.typeCheck(context, Value.Text.class);
    }

    @Override
    public void execute(Context ctx, Map<String, Value> context) throws TriggerValueTypeException, UnknownObjectException, RuleExecutionException {
        Value.Text resolvedTitle = (Value.Text) title.resolve(context);
        Value.Text resolvedText = (Value.Text) text.resolve(context);

        Notification notification = new Notification.Builder(ctx)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(resolvedTitle.getText())
                .setContentText(resolvedText.getText())
                .build();
        NotificationManager manager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, notification);
    }

    @Override
    public String toHumanString() {
        return "show a notification with title " + title;
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(Action.OBJECT, channel.getUrl());
        json.put(Action.METHOD, NotificationManagerChannelFactory.POST_NOTIFICATION);

        JSONArray jsonParams = new JSONArray();
        jsonParams.put(title.toJSON(NotificationManagerChannelFactory.TITLE));
        jsonParams.put(text.toJSON(NotificationManagerChannelFactory.TEXT));
        json.put(Action.PARAMS, jsonParams);
        return json;
    }
}
