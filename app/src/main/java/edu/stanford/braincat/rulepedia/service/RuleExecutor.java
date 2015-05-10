package edu.stanford.braincat.rulepedia.service;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.stanford.braincat.rulepedia.events.EventSource;
import edu.stanford.braincat.rulepedia.exceptions.RuleExecutionException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;
import edu.stanford.braincat.rulepedia.model.Rule;
import edu.stanford.braincat.rulepedia.model.RuleDatabase;

/**
 * Created by gcampagn on 5/2/15.
 */
public class RuleExecutor extends Handler {
    private final Context context;
    private final Set<EventSource> eventSources;

    public RuleExecutor(Context ctx, Looper looper) {
        super(looper);
        context = ctx;
        eventSources = new HashSet<>();
    }

    public void installRule(final JSONObject jsonRule) {
        post(new Runnable() {
            public void run() {
                doInstallRule(jsonRule);
            }
        });
    }

    private void doInstallRule(final JSONObject jsonRule) {
        try {
            Rule rule = RuleDatabase.get().addRule(jsonRule);
            rule.resolve();

            boolean anyFailed = false;
            Collection<EventSource> sources = rule.getEventSources();
            for (EventSource s : sources) {
                try {
                    s.install(context, this);
                } catch (IOException e) {
                    Log.e(RuleExecutorService.LOG_TAG, "Failed to install event source " + s.toString() + ": " + e.getMessage());
                    anyFailed = true;
                }
            }
            if (!anyFailed)
                eventSources.addAll(sources);
        } catch(Exception e) {
            // FIXME: dispatch back to the UI thread
            Log.e(RuleExecutorService.LOG_TAG, "Failed to add rule to the database: " + e.getMessage());
        }
    }

    public void prepare() {
        for (Rule r : RuleDatabase.get().getAllRules()) {
            try {
                r.resolve();
                eventSources.addAll(r.getEventSources());
            } catch (UnknownObjectException e) {
                Log.i(RuleExecutorService.LOG_TAG, "Failed to resolve rule: " + e.getMessage());
                r.setEnabled(false);
            }
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
        for (Rule r : RuleDatabase.get().getAllRules()) {
            try {
                r.updateTrigger();
            } catch (RuleExecutionException e) {
                // FIXME: notify the user!
                Log.e(RuleExecutorService.LOG_TAG, "Failed to update the trigger for rule " + r.toHumanString() + ": " + e.getMessage());
            }
        }
    }

    private void dispatchRules() {
        for (Rule r : RuleDatabase.get().getAllRules()) {
            try {
                if (r.isFiring())
                    r.fire(context);
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
