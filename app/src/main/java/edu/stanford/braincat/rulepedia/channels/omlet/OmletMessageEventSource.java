package edu.stanford.braincat.rulepedia.channels.omlet;

import android.content.Intent;
import android.os.Message;
import android.os.Messenger;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;

import edu.stanford.braincat.rulepedia.events.MessengerEventSource;

/**
 * Created by gcampagn on 5/26/15.
 */
public class OmletMessageEventSource extends MessengerEventSource {
    private static final long OBJECT_ADDED = 1;

    private final Queue<OmletMessage> messageQueue;

    public OmletMessageEventSource() {
        messageQueue = new ArrayDeque<>();
    }

    public OmletMessage getLastMessage() {
        return messageQueue.element();
    }

    @Override
    protected void handleMessage(Message message) {
        if (message.what == OBJECT_ADDED)
            messageQueue.offer(OmletMessage.fromBundle(message.getData()));
    }

    @Override
    protected Intent createIntent(Messenger messenger) {
        Intent intent = new Intent("mobisocial.intent.action.BIND_SERVICE");
        intent.setPackage("mobisocial.omlet");
        intent.putExtra("mobisocial.intent.extra.OBJECT_RECEIVER", messenger);
        return intent;
    }

    @Override
    public boolean checkEvent() throws IOException {
        return !messageQueue.isEmpty();
    }

    @Override
    public void updateState() throws IOException {
        messageQueue.poll();
    }
}
