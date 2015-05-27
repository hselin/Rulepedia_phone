package edu.stanford.braincat.rulepedia.channels.omlet;

import android.content.Context;
import android.net.Uri;
import android.os.RemoteException;

import edu.stanford.braincat.rulepedia.channels.interfaces.SharePictureAction;
import edu.stanford.braincat.rulepedia.exceptions.RuleExecutionException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;
import edu.stanford.braincat.rulepedia.model.Channel;
import edu.stanford.braincat.rulepedia.model.Contact;
import edu.stanford.braincat.rulepedia.model.Value;
import mobisocial.osm.IOsmService;

/**
 * Created by gcampagn on 5/14/15.
 */
public class OmletSharePictureAction extends SharePictureAction {
    public OmletSharePictureAction(Channel channel, Value destination, Value message) {
        super(channel, destination, message);

    }

    @Override
    protected void sharePicture(Context ctx, Contact contact, Value.DirectPicture picture) throws RuleExecutionException {
        IOsmService service = (IOsmService) ((OmletChannel)getChannel()).getService();

        if (service == null)
            throw new RuleExecutionException("Omlet service not available");

        try {
            service.sendPicture(Uri.parse(contact.getUrl()), Uri.parse(picture.toString()));
        } catch(RemoteException e) {
            throw new RuleExecutionException(e);
        }
    }

    @Override
    public void resolve() throws UnknownObjectException {
        Channel newChannel = getChannel().resolve();
        if (!(newChannel instanceof OmletChannel))
            throw new UnknownObjectException(newChannel.getUrl());
        setChannel(newChannel);
    }
}
