package edu.stanford.braincat.rulepedia.channels.googlefit;

import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.Fitness;

import java.lang.ref.WeakReference;

import edu.stanford.braincat.rulepedia.model.Channel;

/**
 * Created by gcampagn on 5/13/15.
 */
public class GoogleFitChannel extends Channel {
    private int clientRefCount;
    private GoogleApiClient client;
    private WeakReference<ActivityMonitorEventSource> activityMonitorSourceRef;

    public GoogleFitChannel(GoogleFitChannelFactory factory, String url) {
        super(factory, url);
        clientRefCount = 0;
    }

    @Override
    public String toHumanString() {
        return "Google Fit";
    }

    GoogleApiClient acquireClient(Context ctx) {
        if (client == null) {
            client = new GoogleApiClient.Builder(ctx)
                    .addApi(Fitness.SESSIONS_API)
                    .addApi(Fitness.HISTORY_API)
                    .addApi(Fitness.SENSORS_API).build();
            client.blockingConnect();
        }

        clientRefCount++;
        return client;
    }

    void releaseClient() {
        if (--clientRefCount == 0) {
            client.disconnect();
            client = null;
        }
    }

    public ActivityMonitorEventSource getActivityMonitorEventSource() {
        ActivityMonitorEventSource source;

        if (activityMonitorSourceRef != null)
            source = activityMonitorSourceRef.get();
        else
            source = null;

        if (source == null) {
            source = new ActivityMonitorEventSource();
            activityMonitorSourceRef = new WeakReference<>(source);
        }

        return source;
    }
}
