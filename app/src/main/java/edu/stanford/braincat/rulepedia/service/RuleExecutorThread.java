package edu.stanford.braincat.rulepedia.service;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.HashSet;
import java.util.Set;

import edu.stanford.braincat.rulepedia.events.EventSource;
import edu.stanford.braincat.rulepedia.exceptions.RuleExecutionException;
import edu.stanford.braincat.rulepedia.model.Rule;
import edu.stanford.braincat.rulepedia.model.RuleDatabase;

/**
 * Created by gcampagn on 4/30/15.
 */
public class RuleExecutorThread extends Thread {
    private final Context context;
    private final EventSource terminationSource;
    private final RuleDatabase database;
    private final Set<EventSource> eventSources;
    private final Selector selector;


    public RuleExecutorThread(Context context, RuleDatabase database, EventSource terminationSource) throws IOException {
        this.context = context;
        this.terminationSource = terminationSource;
        this.database = database;
        this.eventSources = new HashSet<>();
        this.selector = Selector.open();
    }

    private void prepareEventSources() {
        eventSources.add(terminationSource);
        for (Rule r : database.getAllRules()) {
            eventSources.addAll(r.getEventSources());
        }

        Set<EventSource> failedSources = new HashSet<>();
        for (EventSource s : eventSources) {
            try {
                s.install(selector);
            } catch (IOException e) {
                Log.e(RuleExecutorService.LOG_TAG, "Failed to install event source " + s.toString() + ": " + e.getMessage());
                failedSources.add(s);
            }
        }
        eventSources.removeAll(failedSources);
    }

    private void destroyEventSources() {
        for (EventSource s : eventSources) {
            try {
                s.uninstall();
            } catch(IOException e) {
                Log.e(RuleExecutorService.LOG_TAG, "Failed to uninstall event source " + s.toString() + ": " + e.getMessage());
            }
        }
        eventSources.clear();
    }

    private void waitForNextEvent() throws InterruptedException {
        long timeout = Long.MAX_VALUE;

        for (EventSource s : eventSources)
            timeout = Math.min(timeout, s.getTimeout());

        selector.wait(timeout);
    }

    private void updateTriggers() {
        for (Rule r : database.getAllRules()) {
            try {
                r.updateTrigger();
            } catch (IOException e) {
                // FIXME: notify the user!
                Log.e(RuleExecutorService.LOG_TAG, "Failed to update the trigger for rule " + r.toHumanString() + ": " + e.getMessage());
            }
        }
    }

    private void dispatchRules() {
        for (Rule r : database.getAllRules()) {
            if (r.isFiring()) {
                try {
                    r.fire();
                } catch (RuleExecutionException e) {
                    // FIXME: notify the user!
                    Log.e(RuleExecutorService.LOG_TAG, "Failed to run rule " + r.toHumanString() + ": " + e.getMessage());
                }
            }
        }
    }

    private void updateEventSourceState() {
        for (EventSource s : eventSources)
            s.updateState();
    }

    @Override
    public void run() {
        prepareEventSources();

        while (true) {
            try {
                waitForNextEvent();
            } catch(InterruptedException e) {
                // got an interrupt, probably some helper thread
            }

            if (terminationSource.checkEvent())
                break;

            updateTriggers();
            dispatchRules();
            updateEventSourceState();
        }

        destroyEventSources();
    }
}
