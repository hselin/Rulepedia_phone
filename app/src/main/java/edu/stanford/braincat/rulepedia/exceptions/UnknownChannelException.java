package edu.stanford.braincat.rulepedia.exceptions;

/**
 * Created by gcampagn on 4/30/15.
 */
public class UnknownChannelException extends Exception {
    public UnknownChannelException(String id) {
        super("No such action or trigger named " + id);
    }

    public UnknownChannelException(String id, Exception cause) {
        super("No such action or trigger named " + id, cause);
    }
}
