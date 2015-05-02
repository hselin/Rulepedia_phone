package edu.stanford.braincat.rulepedia.exceptions;

/**
 * Created by gcampagn on 5/2/15.
 */
public class TriggerValueTypeException extends Exception {
    public TriggerValueTypeException(String message) {
        super(message);
    }

    public TriggerValueTypeException(Exception wrapped) {
        super(wrapped);
    }
}
