package edu.stanford.braincat.rulepedia.model;

/**
 * Created by gcampagn on 5/30/15.
 */
public class PlaceholderDevice extends Device {
    private final String text;

    public PlaceholderDevice(DeviceFactory factory, String url, String text) {
        super(factory, url);
        this.text = text;
    }

    @Override
    public boolean isPlaceholder() {
        return true;
    }

    @Override
    public String toHumanString() {
        return text;
    }
}
