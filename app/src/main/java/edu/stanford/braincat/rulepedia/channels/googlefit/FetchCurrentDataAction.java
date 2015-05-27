package edu.stanford.braincat.rulepedia.channels.googlefit;

import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import edu.stanford.braincat.rulepedia.events.EventSource;
import edu.stanford.braincat.rulepedia.exceptions.RuleExecutionException;
import edu.stanford.braincat.rulepedia.exceptions.TriggerValueTypeException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;
import edu.stanford.braincat.rulepedia.model.Action;
import edu.stanford.braincat.rulepedia.model.Channel;
import edu.stanford.braincat.rulepedia.model.ObjectPool;
import edu.stanford.braincat.rulepedia.model.Value;

/**
 * Created by gcampagn on 5/13/15.
 */
public class FetchCurrentDataAction implements Action {
    private static final String FITNESS_CURRENT_VALUE_PREFIX = "fitness-current-value-";

    private volatile Channel channel;
    private final CurrentDataTypeValue dataType;

    public FetchCurrentDataAction(Channel channel, CurrentDataTypeValue dataType) {
        this.channel = channel;
        this.dataType = dataType;
    }

    @Override
    public Channel getChannel() {
        return channel;
    }

    @Override
    public Collection<EventSource> getEventSources() {
        return Collections.emptySet();
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
        channel = newChannel;
    }

    @Override
    public void typeCheck(Map<String, Class<? extends Value>> context) throws TriggerValueTypeException {
        String dataTypeId = dataType.toString();
        context.put(FITNESS_CURRENT_VALUE_PREFIX + dataTypeId, Value.Number.class);
    }

    @Override
    public void execute(Context ctx, Map<String, Value> context) throws UnknownObjectException, RuleExecutionException {
        GoogleApiClient client = ((GoogleFitChannel) channel).acquireClient(ctx);

        try {
            long now = System.currentTimeMillis();
            DataReadRequest.Builder builder = new DataReadRequest.Builder()
                    .read(dataType.getType())
                    .setTimeRange(now - 24 * 3600 * 1000, now, TimeUnit.MILLISECONDS);

            DataReadResult result = Fitness.HistoryApi.readData(client, builder.build()).await();

            DataSet dataSet = result.getDataSet(dataType.getType());
            List<DataPoint> dataPoints = dataSet.getDataPoints();

            DataPoint latest = null;
            for (DataPoint point : dataPoints) {
                if (latest == null || point.getTimestampNanos() >= latest.getTimestampNanos())
                    latest = point;
            }
            if (latest == null)
                throw new RuleExecutionException("Google API did not return any data");

            com.google.android.gms.fitness.data.Value value = latest.getValue(dataType.getField());

            String dataTypeId = dataType.toString();

            if (value.getFormat() == Field.FORMAT_INT32)
                context.put(FITNESS_CURRENT_VALUE_PREFIX + dataTypeId, new Value.Number(value.asInt()));
            else if (value.getFormat() == Field.FORMAT_FLOAT)
                context.put(FITNESS_CURRENT_VALUE_PREFIX + dataTypeId, new Value.Number(value.asFloat()));
            else
                throw new RuleExecutionException("Google Fit data point has invalid type");
        } finally {
            ((GoogleFitChannel) channel).releaseClient();
        }
    }

    @Override
    public String toHumanString() {
        return "Read my biometric value of " + dataType.toString();
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(Action.OBJECT, channel.getUrl());
        json.put(Action.METHOD, GoogleFitChannelFactory.FETCH_HISTORY);

        JSONArray jsonParams = new JSONArray();
        jsonParams.put(dataType.toJSON(GoogleFitChannelFactory.DATA_TYPE));
        json.put(Action.PARAMS, jsonParams);

        return json;
    }

}
