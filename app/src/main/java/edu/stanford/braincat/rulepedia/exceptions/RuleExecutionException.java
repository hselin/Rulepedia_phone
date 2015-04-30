package edu.stanford.braincat.rulepedia.exceptions;

import edu.stanford.braincat.rulepedia.R;

/**
 * Created by gcampagn on 4/30/15.
 */
public class RuleExecutionException extends Exception {
    public RuleExecutionException(Exception wrapped) {
        // FIXME: translations
        super("Rule execution failed", wrapped);
    }
}
