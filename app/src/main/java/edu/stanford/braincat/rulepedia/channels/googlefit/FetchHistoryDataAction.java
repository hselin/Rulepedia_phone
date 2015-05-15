package edu.stanford.braincat.rulepedia.channels.googlefit;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import edu.stanford.braincat.rulepedia.exceptions.RuleExecutionException;
import edu.stanford.braincat.rulepedia.exceptions.TriggerValueTypeException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;
import edu.stanford.braincat.rulepedia.model.Action;
import edu.stanford.braincat.rulepedia.model.Channel;
import edu.stanford.braincat.rulepedia.model.ObjectPool;
import edu.stanford.braincat.rulepedia.model.Value;
import edu.stanford.braincat.rulepedia.service.RuleExecutorService;

/**
 * Created by gcampagn on 5/13/15.
 */
public class FetchHistoryDataAction implements Action {
    public static final String FITNESS_STATISTICS_PREFIX = "fitness-statistics-";

    private volatile Channel channel;
    private final HistoryDataTypeValue dataType;
    private final Value aggregatePeriod;
    private final Value activityFilter;

    public FetchHistoryDataAction(Channel channel, HistoryDataTypeValue dataType, Value aggregatePeriod, Value activityFilter) {
        this.channel = channel;
        this.dataType = dataType;
        this.aggregatePeriod = aggregatePeriod;
        this.activityFilter = activityFilter;
    }

    @Override
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
        if (!(newChannel instanceof GoogleFitChannel))
            throw new UnknownObjectException(newChannel.getUrl());
        channel = newChannel;
    }

    @Override
    public void typeCheck(Map<String, Class<? extends Value>> context) throws TriggerValueTypeException {
        String dataTypeId = dataType.toString();

        aggregatePeriod.typeCheck(context, Value.Number.class);
        activityFilter.typeCheck(context, Value.Text.class);
        context.put(FITNESS_STATISTICS_PREFIX + dataTypeId, Value.Number.class);
    }

    @Override
    public void execute(Context ctx, Map<String, Value> context) throws UnknownObjectException, RuleExecutionException {
        GoogleApiClient client = ((GoogleFitChannel)channel).acquireClient(ctx);

        try {
            Value.Number resolvedPeriod = (Value.Number) aggregatePeriod.resolve(context);
            Value.Text resolvedActivity;
            if (activityFilter != null)
                resolvedActivity = (Value.Text) activityFilter.resolve(context);
            else
                resolvedActivity = null;

            long now = System.currentTimeMillis();
            DataReadRequest.Builder builder = new DataReadRequest.Builder()
                    .aggregate(dataType.getInput(), dataType.getOutput())
                    .setTimeRange(now - resolvedPeriod.getNumber().longValue(), now, TimeUnit.MILLISECONDS);
            if (resolvedActivity != null)
                builder.bucketByActivityType(resolvedPeriod.getNumber().intValue(), TimeUnit.MILLISECONDS);
            else
                builder.bucketByTime(resolvedPeriod.getNumber().intValue(), TimeUnit.MILLISECONDS);

            DataReadResult result = Fitness.HistoryApi.readData(client, builder.build()).await();
            Bucket bucket = null;
            for (Bucket b : result.getBuckets()) {
                if (resolvedActivity != null && !b.getActivity().equals(resolvedActivity.getText()))
                    continue;

                bucket = b;
                break;
            }
            if (bucket == null)
                throw new RuleExecutionException("No data from Google Fit");

            if (bucket.getEndTime(TimeUnit.MILLISECONDS) - bucket.getStartTime(TimeUnit.MILLISECONDS) < resolvedPeriod.getNumber().longValue() - 60000)
                Log.w(RuleExecutorService.LOG_TAG, "Google Fit data API returned bucket too short");

            DataSet dataSet = bucket.getDataSet(dataType.getOutput());
            List<DataPoint> dataPoints = dataSet.getDataPoints();

            if (dataPoints.size() != 1)
                throw new RuleExecutionException("Google Fit did not bucket the data properly, got " + dataPoints.size() + " points in one bucket");

            com.google.android.gms.fitness.data.Value value = dataPoints.get(0).getValue(dataType.getField());

            String dataTypeId = dataType.toString();

            if (value.getFormat() == Field.FORMAT_INT32)
                context.put(FITNESS_STATISTICS_PREFIX + dataTypeId, new Value.Number(value.asInt()));
            else if (value.getFormat() == Field.FORMAT_FLOAT)
                context.put(FITNESS_STATISTICS_PREFIX + dataTypeId, new Value.Number(value.asFloat()));
            else
                throw new RuleExecutionException("Google Fit data point has invalid type");
        } finally {
            ((GoogleFitChannel) channel).releaseClient();
        }
    }

    @Override
    public String toHumanString() {
        return "Compute the aggregate statistics of " + dataType + " over the last " + aggregatePeriod.toString();
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(Action.OBJECT, channel.getUrl());
        json.put(Action.METHOD, GoogleFitChannelFactory.FETCH_HISTORY);

        JSONArray jsonParams = new JSONArray();
        jsonParams.put(dataType.toJSON(GoogleFitChannelFactory.DATA_TYPE));
        jsonParams.put(aggregatePeriod.toJSON(GoogleFitChannelFactory.AGGREGATE_PERIOD));
        if (activityFilter != null)
            jsonParams.put(activityFilter.toJSON(GoogleFitChannelFactory.ACTIVITY_FILTER));
        json.put(Action.PARAMS, jsonParams);

        return json;
    }
}
