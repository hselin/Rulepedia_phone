package edu.stanford.braincat.rulepedia.model;

/**
 * Created by gcampagn on 4/30/15.
 */
public abstract class Value {
    public Contact asContact() {
        return (Contact)this;
    }

    public Text asText() {
        return (Text)this;
    }

    public Number asNumber() {
        return (Number)this;
    }

    public static class Contact extends Value {
        private final String rep;

        private Contact(String rep) {
            this.rep = rep;
        }

        public static Contact fromString(String string) {
            return new Contact(string);
        }

        public String toString() {
            return rep;
        }

        public String getContact() {
            return rep;
        }
    }

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
