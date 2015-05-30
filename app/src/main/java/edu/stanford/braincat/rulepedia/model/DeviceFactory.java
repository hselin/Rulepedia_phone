package edu.stanford.braincat.rulepedia.model;

/**
 * Created by gcampagn on 5/30/15.
 */
public abstract class DeviceFactory extends ObjectPool.ObjectFactory<Device> {
    protected DeviceFactory(String prefix) {
        super(prefix);
    }
}
