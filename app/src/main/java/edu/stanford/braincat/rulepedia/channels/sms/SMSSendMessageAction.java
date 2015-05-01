package edu.stanford.braincat.rulepedia.channels.sms;

import edu.stanford.braincat.rulepedia.exceptions.RuleExecutionException;
import edu.stanford.braincat.rulepedia.model.Action;
import edu.stanford.braincat.rulepedia.model.Value;

/**
 * Created by gcampagn on 5/1/15.
 */
public class SMSSendMessageAction implements Action {
    private final SMSChannel channel;
    private final String destination;
    private Value message;

    public SMSSendMessageAction(SMSChannel channel, String destination, Value message) {
        this.channel = channel;
        this.destination = destination;
        this.message = message;
    }

    @Override
    public void execute() throws RuleExecutionException {
        // TODO
    }

    @Override
    public String toHumanString() {
        return "send a message to " + destination;
    }
}
