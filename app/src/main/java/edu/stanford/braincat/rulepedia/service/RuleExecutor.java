package edu.stanford.braincat.rulepedia.service;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import edu.stanford.braincat.rulepedia.events.EventSource;
import edu.stanford.braincat.rulepedia.exceptions.RuleExecutionException;
import edu.stanford.braincat.rulepedia.model.Rule;
import edu.stanford.braincat.rulepedia.model.RuleDatabase;

/**
 * Created by gcampagn on 5/2/15.
 */
public class RuleExecutor extends Handler {
    private final Context context;
    private final RuleDatabase database;
    private final Set<EventSource> eventSources;

    public RuleExecutor(Context ctx, Looper looper, RuleDatabase db) {
        super(looper);
        context = ctx;
        database = db;
        eventSources = new HashSet<>();
    }

    public void prepare() {
        for (Rule r : database.getAllRules()) {
            eventSources.addAll(r.getEventSources());
        }

        Set<EventSource> failedSources = new HashSet<>();
        for (EventSource s : eventSources) {
            try {
                s.install(context, this);
            } catch (IOException e) {
                Log.e(RuleExecutorService.LOG_TAG, "Failed to install event source " + s.toString() + ": " + e.getMessage());
                failedSources.add(s);
            }
        }
        eventSources.removeAll(failedSources);
    }

    public void destroy() {
        for (EventSource s : eventSources) {
            try {
                s.uninstall(context);
            } catch(IOException e) {
                Log.e(RuleExecutorService.LOG_TAG, "Failed to uninstall event source " + s.toString() + ": " + e.getMessage());
            }
        }
        eventSources.clear();
    }

    @Override
    public void handleMessage(Message msg) {
        // dispatch message to event sources
        dispatchMessage(msg);

        // recompute triggers based on the new state of the event sources
        updateTriggers();

        // dispatch any rule that now triggers true
        dispatchRules();

        // clear events and post any newly triggered message, if necessary
        updateEventSourceState();
    }

    private void updateTriggers() {
        for (Rule r : database.getAllRules()) {
            try {
                r.updateTrigger();
            } catch (RuleExecutionException e) {
                // FIXME: notify the user!
                Log.e(RuleExecutorService.LOG_TAG, "Failed to update the trigger for rule " + r.toHumanString() + ": " + e.getMessage());
            }
        }
    }

    private void dispatchRules() {
        for (Rule r : database.getAllRules()) {
            try {
                if (r.isFiring())
                    r.fire();
            } catch (RuleExecutionException e) {
                Log.e(RuleExecutorService.LOG_TAG, "Failed to run rule " + r.toHumanString() + ": " + e.getMessage());
            }
        }
    }

    private void updateEventSourceState() {
        for (EventSource s : eventSources)
            s.updateState();
    }
}
