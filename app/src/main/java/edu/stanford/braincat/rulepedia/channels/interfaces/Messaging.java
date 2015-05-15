package edu.stanford.braincat.rulepedia.channels.interfaces;

/**
 * Created by gcampagn on 5/1/15.
 */
public interface Messaging {
    // channels
    String MESSAGE_RECEIVED = "message-received";
    String SEND_MESSAGE = "send-message";
    String SHARE_PICTURE = "share-picture";

    // filters
    String CONTENT_CONTAINS = "content-contains";
    String SENDER_MATCHES = "sender-matches";

    // trigger/action values
    String SENDER = "sender";
    String DESTINATION = "destination";
    String MESSAGE = "content";
}
