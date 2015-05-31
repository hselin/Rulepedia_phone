package edu.stanford.braincat.rulepedia.exceptions;

/**
 * Created by gcampagn on 4/30/15.
 */
public class UnknownObjectException extends Exception {
    public UnknownObjectException(String url, Exception cause) {
        super("Object referenced by " + url + " is not known", cause);
    }

    public UnknownObjectException(String url) {
        super("Object referenced by " + url + " is not known");
    }

    // For subclasses to change the message
    protected UnknownObjectException(String message, int unused) {
        super(message);
    }
}
