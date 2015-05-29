package edu.stanford.braincat.rulepedia.channels.email;

import android.content.Context;

import java.io.IOException;

import edu.stanford.braincat.rulepedia.channels.android.ContentProviderContact;
import edu.stanford.braincat.rulepedia.channels.interfaces.SendMessageAction;
import edu.stanford.braincat.rulepedia.exceptions.RuleExecutionException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;
import edu.stanford.braincat.rulepedia.model.Channel;
import edu.stanford.braincat.rulepedia.model.Contact;
import edu.stanford.braincat.rulepedia.model.Value;

/**
 * Created by gcampagn on 5/29/15.
 */
public class EmailSendMessageAction extends SendMessageAction {

    public EmailSendMessageAction(Channel channel, Value destination, Value message) {
        super(channel, destination, message);
    }

    @Override
    protected void sendMessage(Context ctx, Contact contact, String message) throws UnknownObjectException, RuleExecutionException {
        String email;
        if (contact instanceof EmailContact)
            email = ((EmailContact) contact).getEmail();
        else if (contact instanceof ContentProviderContact)
            email = ((ContentProviderContact) contact).getEmail(ctx);
        else
            throw new UnknownObjectException(contact.getUrl());
        if (email == null)
            throw new UnknownObjectException(contact.getUrl());

        try {
            EmailSender.sendEmail(email, "Message from Sabrina", message);
        } catch(IOException e) {
            throw new RuleExecutionException(e);
        }
    }

    @Override
    public void resolve() throws UnknownObjectException {
        Channel newChannel = getChannel().resolve();
        if (!(newChannel instanceof EmailChannel))
            throw new UnknownObjectException(newChannel.getUrl());
        setChannel(newChannel);
    }
}
