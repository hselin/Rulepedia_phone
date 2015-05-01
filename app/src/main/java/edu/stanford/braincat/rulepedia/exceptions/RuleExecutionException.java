package edu.stanford.braincat.rulepedia.exceptions;

/**
 * Created by gcampagn on 4/30/15.
 */
public class RuleExecutionException extends Exception {
    public RuleExecutionException(Exception wrapped) {
        // FIXME: translations
        super("Rule execution failed", wrapped);
    }

    public RuleExecutionException(String message, Exception wrapped) {
        super("Rule execution failed: " + message, wrapped);
    }

    public RuleExecutionException(String message){
        super("Rule execution failed: " + message);
    }
}
