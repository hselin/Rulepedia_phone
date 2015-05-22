package edu.stanford.braincat.rulepedia.channels.omlet;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import edu.stanford.braincat.rulepedia.channels.interfaces.SendMessageAction;
import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;
import edu.stanford.braincat.rulepedia.model.Channel;
import edu.stanford.braincat.rulepedia.model.Contact;
import edu.stanford.braincat.rulepedia.model.Value;

/**
 * Created by gcampagn on 5/14/15.
 */
public class OmletSendMessageAction extends SendMessageAction {
    public OmletSendMessageAction(Channel channel, Value destination, Value message) {
        super(channel, destination, message);

    }

    @Override
    protected void sendMessage(Context ctx, Contact contact, String message) {
        // FIXME should be SENDTO
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(contact.getUrl()));
        intent.putExtra(Intent.EXTRA_TEXT, message);
        intent.setType("text/plain");
        intent.setPackage(OmletChannel.OMLET_PACKAGE);
        ctx.startActivity(intent);
    }

    @Override
    public void resolve() throws UnknownObjectException {
        Channel newChannel = getChannel().resolve();
        if (!(newChannel instanceof OmletChannel))
            throw new UnknownObjectException(newChannel.getUrl());
        setChannel(newChannel);
    }
}
