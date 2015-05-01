package edu.stanford.braincat.rulepedia.model;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by gcampagn on 4/30/15.
 */
public class RuleDatabase {
    private final SortedSet<Rule> rules;

    public RuleDatabase() {
        rules = new TreeSet<>(new Comparator<Rule>() {
            @Override
            public int compare(Rule lhs, Rule rhs) {
                // higher priority first
                return rhs.getPriority() - lhs.getPriority();
            }
        });
    }

    public Collection<Rule> getAllRules() {
        return Collections.unmodifiableSortedSet(rules);
    }

    public void load() throws IOException {
        // wire up
    }
}
