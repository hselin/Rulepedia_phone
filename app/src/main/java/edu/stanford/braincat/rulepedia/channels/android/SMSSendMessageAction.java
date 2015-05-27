package edu.stanford.braincat.rulepedia.channels.android;

import android.content.Context;
import android.telephony.SmsManager;

import edu.stanford.braincat.rulepedia.channels.interfaces.SendMessageAction;
import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;
import edu.stanford.braincat.rulepedia.model.Channel;
import edu.stanford.braincat.rulepedia.model.Contact;
import edu.stanford.braincat.rulepedia.model.Value;

/**
 * Created by gcampagn on 5/1/15.
 */
public class SMSSendMessageAction extends SendMessageAction {

    public SMSSendMessageAction(Channel channel, Value destination, Value message) {
        super(channel, destination, message);
    }

    @Override
    protected void sendMessage(Context ctx, Contact contact, String message) throws UnknownObjectException {
        SmsManager smsManager = SmsManager.getDefault();

        String phoneNumber;
        if (contact instanceof SMSContact)
            phoneNumber = ((SMSContact) contact).getAddress();
        else if (contact instanceof ContentProviderContact)
            phoneNumber = ((ContentProviderContact) contact).getPhoneNumber(ctx);
        else
            throw new UnknownObjectException(contact.getUrl());
        if (phoneNumber == null)
            throw new UnknownObjectException(contact.getUrl());

        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
    }

    @Override
    public void resolve() throws UnknownObjectException {
        Channel newChannel = getChannel().resolve();
        if (!(newChannel instanceof SMSChannel))
            throw new UnknownObjectException(newChannel.getUrl());
        setChannel(newChannel);
    }
}
