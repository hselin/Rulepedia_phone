package edu.stanford.braincat.rulepedia.model;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.stanford.braincat.rulepedia.channels.Util;
import edu.stanford.braincat.rulepedia.exceptions.TriggerValueTypeException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownChannelException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;

/**
 * Created by gcampagn on 4/30/15.
 */
public class RuleDatabase {
    private final Map<String, Rule> rules;
    private final SortedSet<Rule> sortedRules;
    private boolean dirty;
    private boolean loaded;

    private final static RuleDatabase instance = new RuleDatabase();

    private RuleDatabase() {
        rules = new HashMap<>();
        sortedRules = new TreeSet<>(new Comparator<Rule>() {
            @Override
            public int compare(Rule lhs, Rule rhs) {
                // higher priority first
                return rhs.getPriority() - lhs.getPriority();
            }
        });
    }

    public static RuleDatabase get() {
        return instance;
    }

    public synchronized boolean isLoaded() {
        return loaded;
    }

    public synchronized Collection<Rule> getAllRules() {
        return Collections.unmodifiableSortedSet(sortedRules);
    }

    public synchronized Rule getRuleById(String id) {
        return rules.get(id);
    }

    private static Value parseParam(ChannelFactory factory, String method, String name, JSONObject jsonParam) throws
            JSONException, UnknownChannelException, TriggerValueTypeException {
        Class<? extends Value> valueType = factory.getParamType(method, name);

        if (jsonParam.has("trigger-value")) {
            return new Value.TriggerValue(jsonParam.getString("trigger-value"), valueType);
        } else {
            try {
                return (Value) valueType.getMethod("fromString", String.class).invoke(null, jsonParam.getString("value"));
            } catch (IllegalAccessException | NoSuchMethodException | ClassCastException e) {
                throw new AssertionError(e);
            } catch (InvocationTargetException e) {
                throw new TriggerValueTypeException(e);
            }
        }
    }

    private static Map<String, Value> parseParams(ChannelFactory factory, String method, JSONArray jsonParams) throws
            JSONException, UnknownChannelException, TriggerValueTypeException {
        Map<String, Value> params = new HashMap<>();

        for (int i = 0; i < jsonParams.length(); i++) {
            JSONObject jsonParam = jsonParams.getJSONObject(i);

            String name = jsonParam.getString("name");
            Value value = parseParam(factory, method, name, jsonParam);
            params.put(name, value);
        }

        return params;
    }

    private static Trigger parseCompositeTrigger(JSONObject jsonTrigger) throws
            JSONException, UnknownObjectException, UnknownChannelException, TriggerValueTypeException {
        ArrayList<Trigger> children = new ArrayList<>();

        String combinator = jsonTrigger.getString(CompositeTrigger.COMBINATOR);
        JSONArray jsonChildren = jsonTrigger.getJSONArray(CompositeTrigger.OPERANDS);

        for (int i = 0; i < jsonChildren.length(); i++) {
            children.add(parseTrigger(jsonChildren.getJSONObject(i)));
        }

        switch (combinator) {
            case CompositeTrigger.And.OP:
                return new CompositeTrigger.And(children);
            case CompositeTrigger.Or.OP:
                return new CompositeTrigger.Or(children);
            default:
                throw new JSONException("Invalid trigger combinator " + combinator);
        }
    }

    private static Trigger parseSingleTrigger(JSONObject jsonTrigger) throws
            JSONException, UnknownObjectException, UnknownChannelException, TriggerValueTypeException {
        String objectUrl = jsonTrigger.getString(Trigger.OBJECT);
        String method = jsonTrigger.getString(Trigger.TRIGGER);

        Channel channel = ChannelPool.get().getObject(objectUrl);
        ChannelFactory factory = channel.getFactory();

        return factory.createTrigger(channel, method, parseParams(factory, method, jsonTrigger.getJSONArray(Trigger.PARAMS)));
    }

    private static Trigger parseTrigger(JSONObject jsonTrigger) throws
            JSONException, UnknownObjectException, UnknownChannelException, TriggerValueTypeException {
        if (jsonTrigger.has(CompositeTrigger.COMBINATOR))
            return parseCompositeTrigger(jsonTrigger);
        else
            return parseSingleTrigger(jsonTrigger);
    }

    private static Action parseAction(JSONObject jsonAction) throws
            JSONException, UnknownObjectException, UnknownChannelException, TriggerValueTypeException {
        String objectUrl = jsonAction.getString(Action.OBJECT);
        String method = jsonAction.getString(Action.METHOD);

        Channel channel = ChannelPool.get().getObject(objectUrl);
        ChannelFactory factory = channel.getFactory();

        return factory.createAction(channel, method, parseParams(factory, method, jsonAction.getJSONArray(Action.PARAMS)));
    }

    private static Collection<Action> parseActionList(JSONArray jsonActions) throws
            JSONException, UnknownObjectException, UnknownChannelException, TriggerValueTypeException {
        ArrayList<Action> actions = new ArrayList<>();

        for (int i = 0; i < jsonActions.length(); i++)
            actions.add(parseAction(jsonActions.getJSONObject(i)));

        return actions;
    }

    private static Rule parseRule(JSONObject jsonRule) throws
            JSONException, UnknownObjectException, UnknownChannelException, TriggerValueTypeException {
        String name = jsonRule.getString(Rule.NAME);
        String description = jsonRule.getString(Rule.DESCRIPTION);

        JSONObject jsonTrigger = jsonRule.getJSONObject(Rule.TRIGGER);
        Trigger trigger = parseTrigger(jsonTrigger);

        JSONArray jsonActions = jsonRule.getJSONArray(Rule.ACTIONS);
        Collection<Action> actions = parseActionList(jsonActions);

        Rule rule = new Rule(name, description, trigger, actions);
        rule.typeCheck();

        if (!jsonRule.has("id")) {
            rule.setId(Util.toSHA1(rule.toJSON().toString()));
        } else {
            jsonRule.getString("id");
        }

        return rule;
    }

    private void loadRule(JSONObject jsonRule, int position) throws
            JSONException, UnknownObjectException, UnknownChannelException, TriggerValueTypeException {
        Rule rule = parseRule(jsonRule);
        // -position because lower position is higher priority
        rule.setPriority(-position);
        if (jsonRule.has(Rule.ENABLED))
            rule.setEnabled(jsonRule.getBoolean(Rule.ENABLED));
        else
            rule.setEnabled(true);

        rules.put(rule.getId(), rule);
        sortedRules.add(rule);
    }

    public synchronized void load(Context ctx) throws IOException, UnknownObjectException, UnknownChannelException {
        if (loaded)
            return;

        loaded = true;
        try (FileInputStream file = ctx.openFileInput("rules.json")) {
            try {
                JSONArray root = (JSONArray) Util.readJSON(file).nextValue();

                for (int i = 0; i < root.length(); i++)
                    loadRule(root.getJSONObject(i), i);
            } catch (TriggerValueTypeException | NullPointerException | ClassCastException | JSONException e) {
                throw new IOException("Invalid database format on disk", e);
            }
        } catch (FileNotFoundException e) {
            // if there is no file, it's all good
        }
    }

    public synchronized void save(Context ctx) throws IOException {
        if (!dirty)
            return;

        try (FileOutputStream file = ctx.openFileOutput("rules.json", Context.MODE_PRIVATE)) {
            try {
                JSONArray allRules = new JSONArray();

                for (Rule r : sortedRules) {
                    allRules.put(r.toJSON());
                }

                Util.writeJSON(file, allRules);
            } catch (JSONException e) {
                throw new IOException("Failed to serialize db to json: " + e.getMessage());
            }
        }
    }

    public synchronized Rule addRule(JSONObject jsonRule) throws
            JSONException, UnknownObjectException, UnknownChannelException, TriggerValueTypeException {
        Rule rule = parseRule(jsonRule);

        rule.setEnabled(true);
        // FIXME: verify...
        rule.setPriority(rules.size());

        rules.put(rule.getId(), rule);
        sortedRules.add(rule);
        dirty = true;

        return rule;
    }

    public synchronized void removeRule(Rule rule) {
        rules.remove(rule.getId());
        sortedRules.remove(rule);
        dirty = true;
    }
}
