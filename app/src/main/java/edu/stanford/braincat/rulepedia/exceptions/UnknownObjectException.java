package edu.stanford.braincat.rulepedia.exceptions;

/**
 * Created by gcampagn on 4/30/15.
 */
public class UnknownObjectException extends Exception {
    public UnknownObjectException(String url) {
        super("Channel referenced by " + url + " is not known");
    }
}
