package edu.stanford.braincat.rulepedia.model;

/**
 * Created by gcampagn on 4/30/15.
 */
public abstract class Value {
    public static class Text extends Value {
        private final String rep;

        private Text(String rep) {
            this.rep = rep;
        }

        public static Text fromString(String string) {
            return new Text(string);
        }

        public String toString() {
            return rep;
        }

        public String getText() {
            return rep;
        }
    }

    public static class Number extends Value {
        private final String rep; // FIXME

        private Number(String rep) {
            this.rep = rep;
        }

        public static Text fromString(String string) {
            return new Text(string);
        }

        public String toString() {
            return rep;
        }
    }
}
