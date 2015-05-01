package edu.stanford.braincat.rulepedia.model;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

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

    public void loadForExecution() throws IOException, UnknownObjectException, UnknownChannelException {
        triggerdb.load();
        actiondb.load();

        // FIXME: actually load the rules
    }

    public void loadForDisplay() throws IOException, UnknownObjectException, UnknownChannelException {
        triggerdb.load();
        actiondb.load();

        // FIXME: actually load the rules, but don't resolve placeholders and trigger values
    }
}
