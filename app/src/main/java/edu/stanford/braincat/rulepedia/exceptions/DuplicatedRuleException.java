package edu.stanford.braincat.rulepedia.exceptions;

/**
 * Created by braincat on 5/18/15.
 */
public class DuplicatedRuleException extends Exception {
    public DuplicatedRuleException(Exception wrapped) {
        // FIXME: translations
        super("Duplicated rule", wrapped);
    }

    public DuplicatedRuleException(String message, Exception wrapped) {
        super("Duplicated rule: " + message, wrapped);
    }

    public DuplicatedRuleException() {
        super("Duplicated rule");
    }
}
