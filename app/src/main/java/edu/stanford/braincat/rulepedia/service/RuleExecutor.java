package edu.stanford.braincat.rulepedia.service;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.stanford.braincat.rulepedia.BuildConfig;
import edu.stanford.braincat.rulepedia.events.EventSource;
import edu.stanford.braincat.rulepedia.events.EventSourceHandler;
import edu.stanford.braincat.rulepedia.exceptions.DuplicatedRuleException;
import edu.stanford.braincat.rulepedia.exceptions.RuleExecutionException;
import edu.stanford.braincat.rulepedia.exceptions.TriggerValueTypeException;
import edu.stanford.braincat.rulepedia.exceptions.UnexpectedPlaceholderException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownChannelException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;
import edu.stanford.braincat.rulepedia.model.Channel;
import edu.stanford.braincat.rulepedia.model.ObjectDatabase;
import edu.stanford.braincat.rulepedia.model.Rule;
import edu.stanford.braincat.rulepedia.model.RuleDatabase;

/**
 * Created by gcampagn on 5/2/15.
 */
public class RuleExecutor extends EventSourceHandler {
    private final Context context;
    private final Set<EventSource> eventSources;
    private final Set<Channel> channels;

    public RuleExecutor(Context ctx, Looper looper) {
        super(looper);
        context = ctx;
        eventSources = new HashSet<>();
        channels = new HashSet<>();

        try {
            ObjectDatabase.get().load(ctx);
            RuleDatabase.get().load(ctx);
        } catch (IOException e) {
            Log.e(RuleExecutorService.LOG_TAG, "Failed to load database", e);
        }
    }

    public void save() throws IOException {
        ObjectDatabase.get().save(context);
        RuleDatabase.get().save(context);
    }

    public void installRule(final JSONObject jsonRule, final edu.stanford.braincat.rulepedia.service.Callback<Rule> callback) {
        post(new Runnable() {
            public void run() {
                doInstallRule(jsonRule, callback);
            }
        });
    }

    public void reloadRule(final String id, final edu.stanford.braincat.rulepedia.service.Callback<Rule> callback) {
        post(new Runnable() {
            @Override
            public void run() {
                doReloadRule(id, callback);
            }
        });
    }

    public void deleteRule(final String id, final edu.stanford.braincat.rulepedia.service.Callback<Boolean> callback) {
        post(new Runnable() {
            @Override
            public void run() {
                doDeleteRule(id, callback);
            }
        });
    }

    private void doEnableRule(Rule rule) throws UnknownObjectException {
        rule.resolve();

        boolean anyFailed = false;
        boolean anySuccess = false;
        Collection<EventSource> sources = rule.getEventSources();
        for (EventSource s : sources) {
            try {
                s.install(context, this);
                anySuccess = true;
            } catch (IOException e) {
                Log.e(RuleExecutorService.LOG_TAG, "Failed to install event source " + s, e);
                anyFailed = true;
            }
        }
        Collection<Channel> channels = rule.getChannels();
        for (Channel c : channels) {
            try {
                c.enable(context, this);
                anySuccess = true;
            } catch (IOException e) {
                Log.e(RuleExecutorService.LOG_TAG, "Failed to enable channel " + c, e);
                anyFailed = true;
            }
        }
        if (!anyFailed) {
            eventSources.addAll(sources);
            this.channels.addAll(channels);
        }
        if (anySuccess)
            rule.setInstalled(true);
    }


    private void doDisableRule(Rule rule) throws UnknownObjectException {
        rule.resolve();

        boolean anyFailed = false;
        boolean anySuccess = false;
        Collection<EventSource> sources = rule.getEventSources();
        for (EventSource s : sources) {
            try {
                s.uninstall(context);
                anySuccess = true;
            } catch (IOException e) {
                Log.e(RuleExecutorService.LOG_TAG, "Failed to uninstall event source " + s.toString(), e);
                anyFailed = true;
            }
        }
        Collection<Channel> channels = rule.getChannels();
        for (Channel c : channels) {
            try {
                c.disable(context);
            } catch (IOException e) {
                Log.e(RuleExecutorService.LOG_TAG, "Failed to disable channel " + c, e);
                anyFailed = true;
            }
        }
        if (!anyFailed) {
            eventSources.removeAll(sources);
            this.channels.removeAll(channels);
        }
        if (anySuccess)
            rule.setInstalled(false);
    }

    private void doInstallRule(JSONObject jsonRule, edu.stanford.braincat.rulepedia.service.Callback<Rule> callback) {
        try {
            RuleDatabase db = RuleDatabase.get();
            Rule rule = db.addRule(jsonRule);

            try {
                // save eagerly to catch problems
                db.save(context);
            } catch (IOException e) {
                Log.w(RuleExecutorService.LOG_TAG, "Failed to save rule database to disk", e);
                // continue on failure, we'll try again later
            }

            if (BuildConfig.DEBUG && rule.isInstalled())
                throw new AssertionError();
            if (rule.isEnabled())
                doEnableRule(rule);
            callback.post(rule, null);
        } catch (DuplicatedRuleException e) {
            Log.d(RuleExecutorService.LOG_TAG, "Failed to install rule (duplicated)", e);
            callback.post(null, e);
        } catch (UnknownChannelException | TriggerValueTypeException | JSONException e) {
            Log.e(RuleExecutorService.LOG_TAG, "Failed to install rule (parsing problem)", e);
            callback.post(null, e);
        } catch (UnexpectedPlaceholderException e) {
            Log.d(RuleExecutorService.LOG_TAG, "Failed to install rule (contains placeholders)", e);
            callback.post(null, e);
        } catch (UnknownObjectException e) {
            Log.e(RuleExecutorService.LOG_TAG, "Failed to install rule (object resolution problem)", e);
            callback.post(null, e);
        } catch (RuntimeException e) {
            Log.e(RuleExecutorService.LOG_TAG, "RuntimeException while installing rule!", e);
            callback.post(null, e);
        }
    }

    private void doReloadRule(String id, edu.stanford.braincat.rulepedia.service.Callback<Rule> callback) {
        Rule rule = RuleDatabase.get().getRuleById(id);

        if (rule == null) {
            // perfectly legitimate, possible race condition
            Log.i(RuleExecutorService.LOG_TAG, "No rule with id " + id);
            callback.post(null, null);
            return;
        }

        if (rule.isEnabled() == rule.isInstalled()) {
            callback.post(rule, null);
            return;
        }

        try {
            if (rule.isEnabled())
                doEnableRule(rule);
            else
                doDisableRule(rule);
            callback.post(rule, null);
        } catch (UnexpectedPlaceholderException e) {
            Log.d(RuleExecutorService.LOG_TAG, "Failed to reload rule (contains placeholders)", e);
            callback.post(null, e);
        } catch (UnknownObjectException e) {
            Log.e(RuleExecutorService.LOG_TAG, "Failed to reload rule (object resolution problem)", e);
            callback.post(null, e);
        } catch (RuntimeException e) {
            Log.e(RuleExecutorService.LOG_TAG, "RuntimeException while reloading rule!", e);
            callback.post(null, e);
        }
    }

    private void doDeleteRule(String id, edu.stanford.braincat.rulepedia.service.Callback<Boolean> callback) {
        RuleDatabase db = RuleDatabase.get();
        Rule rule = db.getRuleById(id);

        if (rule == null) {
            // perfectly legitimate, possible race condition
            Log.i(RuleExecutorService.LOG_TAG, "No rule with id " + id);
            callback.post(true, null);
            return;
        }

        db.removeRule(rule);

        try {
            // save eagerly to catch problems
            db.save(context);
        } catch (IOException e) {
            Log.w(RuleExecutorService.LOG_TAG, "Failed to save rule database to disk", e);
            // continue on failure, we'll try again later
        }

        if (!rule.isInstalled()) {
            callback.post(true, null);
            return;
        }

        try {
            doDisableRule(rule);
            callback.post(true, null);
        } catch (UnexpectedPlaceholderException e) {
            Log.d(RuleExecutorService.LOG_TAG, "Failed to delete rule (contains placeholders)", e);
            callback.post(null, e);
        } catch (UnknownObjectException e) {
            Log.e(RuleExecutorService.LOG_TAG, "Failed to delete rule (object resolution problem)", e);
            callback.post(null, e);
        } catch (RuntimeException e) {
            Log.e(RuleExecutorService.LOG_TAG, "RuntimeException while deleting rule!", e);
            callback.post(null, e);
        }
    }

    public void prepare() {
        for (Rule r : RuleDatabase.get().getAllRules()) {
            if (!r.isEnabled())
                continue;

            try {
                doEnableRule(r);
            } catch (UnknownObjectException e) {
                Log.i(RuleExecutorService.LOG_TAG, "Failed to bootstrap rule", e);
            }
        }
    }

    public void destroy() {
        for (EventSource s : eventSources) {
            try {
                s.uninstall(context);
            } catch (IOException e) {
                Log.e(RuleExecutorService.LOG_TAG, "Failed to uninstall event source " + s.toString(), e);
            }
        }
        eventSources.clear();

        for (Channel c : channels) {
            try {
                c.disable(context);
            } catch (IOException e) {
                Log.e(RuleExecutorService.LOG_TAG, "Failed to uninstall channel " + c.toString(), e);
            }
        }
        channels.clear();

        for (Rule r : RuleDatabase.get().getAllRules())
            r.setInstalled(false);
    }

    @Override
    public void messageReceived() {
        // recompute triggers based on the new state of the event sources
        updateTriggers();

        // dispatch any rule that now triggers true
        dispatchRules();

        // clear events and post any newly triggered message, if necessary
        updateEventSourceState();
    }

    private void updateTriggers() {
        for (Rule r : RuleDatabase.get().getAllRules()) {
            if (!r.isInstalled())
                    continue;

            try {
                r.updateTrigger(context);
            } catch (RuleExecutionException e) {
                // FIXME: notify the user!
                Log.e(RuleExecutorService.LOG_TAG, "Failed to update the trigger for rule " + r.toHumanString(), e);
            } catch (RuntimeException e) {
                Log.e(RuleExecutorService.LOG_TAG, "RuntimeException while updating trigger for rule " + r.toHumanString(), e);
            }
        }
    }

    private void dispatchRules() {
        for (Rule r : RuleDatabase.get().getAllRules()) {
            if (!r.isInstalled())
                continue;

            try {
                if (r.isFiring())
                    r.fire(context);
            } catch (RuleExecutionException e) {
                Log.e(RuleExecutorService.LOG_TAG, "Failed to run rule " + r.toHumanString(), e);
            } catch (RuntimeException e) {
                Log.e(RuleExecutorService.LOG_TAG, "RuntimeException while running rule " + r.toHumanString(), e);
            }
        }
    }

    private void updateEventSourceState() {
        for (EventSource s : eventSources) {
            try {
                s.updateState();
            } catch (IOException e) {
                Log.e(RuleExecutorService.LOG_TAG, "Failed to update event source " + s.toString(), e);
            }
        }
    }
}
