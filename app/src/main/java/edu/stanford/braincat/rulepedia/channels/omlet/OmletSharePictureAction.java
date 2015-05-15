package edu.stanford.braincat.rulepedia.channels.omlet;

import android.content.Context;
import android.content.Intent;

import edu.stanford.braincat.rulepedia.channels.interfaces.SharePictureAction;
import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;
import edu.stanford.braincat.rulepedia.model.Channel;
import edu.stanford.braincat.rulepedia.model.Contact;
import edu.stanford.braincat.rulepedia.model.Value;

/**
 * Created by gcampagn on 5/14/15.
 */
public class OmletSharePictureAction extends SharePictureAction {
    public OmletSharePictureAction(Channel channel, Value destination, Value message) {
        super(channel, destination, message);

    }

    @Override
    protected void sharePicture(Context ctx, Contact contact, Value.Picture picture) {
        // FIXME should be SENDTO
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, picture.toString());
        intent.setType("image/png");
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
