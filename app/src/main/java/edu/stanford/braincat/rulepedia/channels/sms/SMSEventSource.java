package edu.stanford.braincat.rulepedia.channels.sms;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

import edu.stanford.braincat.rulepedia.events.EventSource;
import edu.stanford.braincat.rulepedia.events.IntentEventSource;

/**
 * Created by gcampagn on 5/1/15.
 */
public class SMSEventSource implements EventSource {
    private boolean installed;
    private final IntentEventSource intentSource;
    private final Queue<SmsMessage> messageQueue;

    public SMSEventSource() {
        intentSource = new IntentEventSource(new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION));
        messageQueue = new ArrayDeque<>();
        installed = false;
    }

    public SmsMessage getLastMessage() {
        if (messageQueue.isEmpty())
            parseReceivedIntent();
        return messageQueue.element();
    }

    private void parseReceivedIntent() {
        Intent intent = intentSource.getLastIntent();
        messageQueue.addAll(Arrays.asList(Telephony.Sms.Intents.getMessagesFromIntent(intent)));
    }

    @Override
    public void install(Context ctx, Handler handler) throws IOException {
        if (!ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY))
            return;
        installed = true;
        intentSource.install(ctx, handler);
    }

    @Override
    public void uninstall(Context ctx) throws IOException {
        if (installed)
            intentSource.uninstall(ctx);
        installed = false;
    }

    @Override
    public boolean checkEvent() {
        return !messageQueue.isEmpty() || intentSource.checkEvent();
    }

    @Override
    public void updateState() {
        messageQueue.poll();
        if (messageQueue.isEmpty())
            intentSource.updateState();
    }
}
