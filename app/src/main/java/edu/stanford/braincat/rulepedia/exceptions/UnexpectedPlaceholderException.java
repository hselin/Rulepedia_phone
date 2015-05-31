package edu.stanford.braincat.rulepedia.exceptions;

/**
 * Created by gcampagn on 5/30/15.
 */
public class UnexpectedPlaceholderException extends UnknownObjectException {
    public UnexpectedPlaceholderException(String url) {
        super("Unexpected placeholder object " + url, 0);
    }
}
