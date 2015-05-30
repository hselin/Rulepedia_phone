package edu.stanford.braincat.rulepedia.model;

/**
 * Created by gcampagn on 5/30/15.
 */
public abstract class Device extends ObjectPool.Object<Device, DeviceFactory> {
    protected Device(DeviceFactory factory, String url) {
        super(factory, url);
    }
}
