package edu.stanford.braincat.rulepedia.model;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.stanford.braincat.rulepedia.channels.Util;
import edu.stanford.braincat.rulepedia.exceptions.UnknownChannelException;
import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;

/**
 * Created by gcampagn on 4/30/15.
 */
public class RuleDatabase {
    private final SortedSet<Rule> rules;
    private final ChannelDatabase<Trigger> triggerdb;
    private final ChannelDatabase<Action> actiondb;
    private final ObjectPool objectdb;

    public RuleDatabase() {
        rules = new TreeSet<>(new Comparator<Rule>() {
            @Override
            public int compare(Rule lhs, Rule rhs) {
                // higher priority first
                return rhs.getPriority() - lhs.getPriority();
            }
        });

        objectdb = new ObjectPool();
        triggerdb = new ChannelDatabase.TriggerDatabase();
        actiondb = new ChannelDatabase.ActionDatabase();
    }

    public Collection<Rule> getAllRules() {
        return Collections.unmodifiableSortedSet(rules);
    }

    private Map<String, Value> parseParams(JSONArray jsonParams) {
        // TODO: implement me
        return new HashMap<>();
    }

    private Trigger parseCompositeTrigger(JSONObject jsonTrigger, boolean resolve) throws JSONException, UnknownObjectException, UnknownChannelException {
        ArrayList<Trigger> children = new ArrayList<>();

        String combinator = jsonTrigger.getString("combinator");
        JSONArray jsonChildren = jsonTrigger.getJSONArray("operand");

        for (int i = 0; i < jsonChildren.length(); i++) {
            children.add(parseTrigger(jsonChildren.getJSONObject(i), resolve));
        }

        switch (combinator) {
            case "and":
                return new CompositeTrigger.And(children);
            case "or":
                return new CompositeTrigger.Or(children);
            default:
                throw new JSONException("Invalid trigger combinator " + combinator);
        }
    }

    private Trigger parseSingleTrigger(JSONObject jsonTrigger, boolean resolve) throws JSONException, UnknownObjectException, UnknownChannelException {
        String objectUrl = jsonTrigger.getString("object");
        String method = jsonTrigger.getString("trigger");

        ObjectPool.Object object = objectdb.getObject(objectUrl);
        if (resolve)
            object.resolve();

        return triggerdb.createChannel(object.getType(), method, object, parseParams(jsonTrigger.getJSONArray("params")));
    }

    private Trigger parseTrigger(JSONObject jsonTrigger, boolean resolve) throws JSONException, UnknownObjectException, UnknownChannelException {
        if (jsonTrigger.has("combinator"))
            return parseCompositeTrigger(jsonTrigger, resolve);
        else
            return parseSingleTrigger(jsonTrigger, resolve);
    }

    private Action parseAction(JSONObject jsonAction, boolean resolve) throws JSONException, UnknownObjectException, UnknownChannelException {
        String objectUrl = jsonAction.getString("object");
        String method = jsonAction.getString("method");

        ObjectPool.Object object = objectdb.getObject(objectUrl);
        if (resolve)
            object.resolve();

        return actiondb.createChannel(object.getType(), method, object, parseParams(jsonAction.getJSONArray("params")));
    }

    private Collection<Action> parseActionList(JSONArray jsonActions, boolean resolve) throws JSONException, UnknownObjectException, UnknownChannelException {
        ArrayList<Action> actions = new ArrayList<>();

        for (int i = 0; i < jsonActions.length(); i++)
            actions.add(parseAction(jsonActions.getJSONObject(i), resolve));

        return actions;
    }

    private void loadRule(JSONObject jsonRule, int position, boolean resolve) throws JSONException, UnknownObjectException, UnknownChannelException {
        String name = jsonRule.getString("name");
        // just to validate the format, we don't do anything with it really
        String description = jsonRule.getString("description");

        JSONObject jsonTrigger = jsonRule.getJSONObject("trigger");
        Trigger trigger = parseTrigger(jsonTrigger, resolve);

        JSONArray jsonActions = jsonRule.getJSONArray("actions");
        Collection<Action> actions = parseActionList(jsonActions, resolve);

        // -position because lower position is higher priority
        rules.add(new Rule(name, trigger, actions, -position));
    }

    private void loadHelper(Context ctx, boolean resolve) throws IOException, UnknownObjectException, UnknownChannelException {
        FileInputStream file = null;
        try {
            file = ctx.openFileInput("rules.json");

            try {
                JSONArray root = (JSONArray) Util.readJSON(file).nextValue();

                for (int i = 0; i < root.length(); i++)
                    loadRule(root.getJSONObject(i), i, resolve);
            } catch (ClassCastException cce) {
                throw new IOException("Invalid database format on disk", cce);
            } catch (JSONException je) {
                throw new IOException("Invalid database format on disk", je);
            }
        } catch(FileNotFoundException fnfe) {
            // if there is no file, it's all good
        } finally {
            if (file != null)
                file.close();
        }
    }

    public void loadForExecution(Context ctx) throws IOException, UnknownObjectException, UnknownChannelException {
        triggerdb.load();
        actiondb.load();

        loadHelper(ctx, true);
    }

    public void loadForDisplay(Context ctx) throws IOException, UnknownObjectException, UnknownChannelException {
        triggerdb.load();
        actiondb.load();

        loadHelper(ctx, false);
    }
}
