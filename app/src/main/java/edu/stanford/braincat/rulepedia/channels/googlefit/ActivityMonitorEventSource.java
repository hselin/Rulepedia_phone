package edu.stanford.braincat.rulepedia.channels.googlefit;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Session;

import java.io.IOException;

import edu.stanford.braincat.rulepedia.events.EventSource;
import edu.stanford.braincat.rulepedia.events.IntentEventSource;

/**
 * Created by gcampagn on 5/13/15.
 */
public class ActivityMonitorEventSource implements EventSource {
    public static final String INTENT = "edu.stanford.braincat.rulepedia.channels.googlefit.ACTIVITY";

    public static class Event {
        public enum Type {
            ACTIVITY_START, ACTIVITY_END
        };

        private final Type type;
        private final Session session;

        private Event(Type type, Session session) {
            this.type = type;
            this.session = session;
        }

        public Type getType() {
            return type;
        }

        public Session getSession() {
            return session;
        }
    }

    private final IntentEventSource intentSource;
    private final GoogleFitChannel channel;
    private Event cachedEvent;
    private GoogleApiClient client;
    private PendingIntent pendingIntent;

    public ActivityMonitorEventSource(GoogleFitChannel channel) {
        intentSource = new IntentEventSource(new IntentFilter(INTENT));
        this.channel = channel;
    }

    @Override
    public void install(Context ctx, Handler handler) throws IOException {
        client = channel.acquireClient(ctx);

        Intent intent = new Intent(ctx, IntentEventSource.EventSourceBroadcastReceiver.class);
        intent.setAction(INTENT);
        pendingIntent = PendingIntent.getBroadcast(ctx, 0, intent, 0);
        Fitness.SessionsApi.registerForSessions(client, pendingIntent);
    }

    @Override
    public void uninstall(Context ctx) throws IOException {
        Fitness.SessionsApi.unregisterForSessions(client, pendingIntent);
        pendingIntent = null;
        client = null;
        channel.releaseClient();
    }

    public Event getLastEvent() {
        if (cachedEvent == null)
            return cachedEvent = parseReceivedIntent();
        else
            return cachedEvent;
    }

    private Event parseReceivedIntent() {
        Intent intent = intentSource.getLastIntent();
        Session session = Session.extract(intent);

        if (session.isOngoing())
            return new Event(Event.Type.ACTIVITY_START, session);
        else
            return new Event(Event.Type.ACTIVITY_END, session);
    }

    @Override
    public boolean checkEvent() {
        return cachedEvent != null || intentSource.checkEvent();
    }

    @Override
    public void updateState() {
        cachedEvent = null;
        intentSource.updateState();
    }
}