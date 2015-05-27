package edu.stanford.braincat.rulepedia.channels.omlet;

import android.content.Context;
import android.net.Uri;
import android.os.RemoteException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import edu.stanford.braincat.rulepedia.channels.interfaces.SendMessageAction;
import edu.stanford.braincat.rulepedia.events.EventSource;
import edu.stanford.braincat.rulepedia.exceptions.RuleExecutionException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;
import edu.stanford.braincat.rulepedia.model.Channel;
import edu.stanford.braincat.rulepedia.model.Contact;
import edu.stanford.braincat.rulepedia.model.Value;
import mobisocial.osm.IOsmService;

/**
 * Created by gcampagn on 5/14/15.
 */
public class OmletSendMessageAction extends SendMessageAction {
    public OmletSendMessageAction(Channel channel, Value destination, Value message) {
        super(channel, destination, message);
    }

    @Override
    public Collection<EventSource> getEventSources() {
        Channel currentChannel = getChannel();
        if (currentChannel instanceof OmletChannel)
            return Arrays.asList(new EventSource[]{((OmletChannel) currentChannel).getEventSource()});
        else
            return Collections.emptySet();
    }

    @Override
    protected void sendMessage(Context ctx, Contact contact, String message) throws RuleExecutionException {
        IOsmService service = (IOsmService) ((OmletChannel)getChannel()).getEventSource().getService();

        if (service == null)
            throw new RuleExecutionException("Omlet service not available");

        try {
            service.sendText(Uri.parse("http://foo"), message);
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
