package edu.stanford.braincat.rulepedia.channels.googlefit;

import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;

import edu.stanford.braincat.rulepedia.exceptions.TriggerValueTypeException;
import edu.stanford.braincat.rulepedia.model.Value;

/**
 * Created by gcampagn on 5/13/15.
 */
public class CurrentDataTypeValue extends Value {
    private final String rep;
    private final DataType type;
    private final Field field;

    public CurrentDataTypeValue(String rep, DataType type, Field field) {
        this.rep = rep;
        this.type = type;
        this.field = field;
    }

    public String toString() {
        return rep;
    }

    public DataType getType() {
        return type;
    }

    public Field getField() {
        return field;
    }

    public static CurrentDataTypeValue fromString(String rep) throws TriggerValueTypeException {
        switch(rep) {
            case "weight":
                return new CurrentDataTypeValue(rep, DataType.TYPE_WEIGHT, Field.FIELD_WEIGHT);
            case "height":
                return new CurrentDataTypeValue(rep, DataType.TYPE_HEIGHT, Field.FIELD_HEIGHT);
            case "basal-metabolic-rate":
                return new CurrentDataTypeValue(rep, DataType.TYPE_BASAL_METABOLIC_RATE, Field.FIELD_CALORIES);
            case "body-fat-percentage":
                return new CurrentDataTypeValue(rep, DataType.TYPE_BODY_FAT_PERCENTAGE, Field.FIELD_PERCENTAGE);
            default:
                throw new TriggerValueTypeException("invalid current data type " + rep);
        }
    }
}
