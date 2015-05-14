package edu.stanford.braincat.rulepedia.channels.googlefit;

import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;

import edu.stanford.braincat.rulepedia.exceptions.TriggerValueTypeException;
import edu.stanford.braincat.rulepedia.model.Value;

/**
 * Created by gcampagn on 5/13/15.
 */
public class HistoryDataTypeValue extends Value {
    private final String rep;
    private final DataType input;
    private final DataType output;
    private final Field field;

    public HistoryDataTypeValue(String rep, DataType input, DataType output, Field field) {
        this.rep = rep;
        this.input = input;
        this.output = output;
        this.field = field;
    }

    public Field getField() {
        return field;
    }

    public DataType getInput() {
        return input;
    }

    public DataType getOutput() {
        return output;
    }

    public String toString() {
        return rep;
    }

    public static HistoryDataTypeValue fromString(String rep) throws TriggerValueTypeException {
        switch (rep) {
            case "calories-expended":
                return new HistoryDataTypeValue(rep, DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED, Field.FIELD_CALORIES);
            case "distance-delta":
                return new HistoryDataTypeValue(rep, DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA, Field.FIELD_DISTANCE);
            case "avg-heart-rate":
                return new HistoryDataTypeValue(rep, DataType.TYPE_HEART_RATE_BPM, DataType.AGGREGATE_HEART_RATE_SUMMARY, Field.FIELD_AVERAGE);
            case "max-heart-rate":
                return new HistoryDataTypeValue(rep, DataType.TYPE_HEART_RATE_BPM, DataType.AGGREGATE_HEART_RATE_SUMMARY, Field.FIELD_MAX);
            case "avg-power":
                return new HistoryDataTypeValue(rep, DataType.TYPE_POWER_SAMPLE, DataType.AGGREGATE_POWER_SUMMARY, Field.FIELD_AVERAGE);
            case "max-power":
                return new HistoryDataTypeValue(rep, DataType.TYPE_POWER_SAMPLE, DataType.AGGREGATE_POWER_SUMMARY, Field.FIELD_MAX);
            case "avg-speed":
                return new HistoryDataTypeValue(rep, DataType.TYPE_SPEED, DataType.AGGREGATE_SPEED_SUMMARY, Field.FIELD_AVERAGE);
            case "max-speed":
                return new HistoryDataTypeValue(rep, DataType.TYPE_SPEED, DataType.AGGREGATE_SPEED_SUMMARY, Field.FIELD_MAX);
            case "step-count":
                return new HistoryDataTypeValue(rep, DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA, Field.FIELD_STEPS);
            default:
                throw new TriggerValueTypeException("invalid history data type " + rep);
        }
    }
}
