package edu.stanford.braincat.rulepedia.model;

import edu.stanford.braincat.rulepedia.channels.ibeacon.IBeaconDeviceFactory;

/**
 * Created by gcampagn on 5/30/15.
 */
public class DevicePool extends ObjectPool<Device, DeviceFactory> {
    public static final String KIND = "device";
    public static final String PREFIX = ObjectPool.PREFIX + KIND + "/";
    public static final String PLACEHOLDER_PREFIX = ObjectPool.PLACEHOLDER_PREFIX + KIND + "/";

    private static final DevicePool instance = new DevicePool();

    public static DevicePool get() {
        return instance;
    }

    private DevicePool() {
        super(KIND);

        registerFactory(new IBeaconDeviceFactory());
    }

    public DeviceFactory getFactory(String type) {
        return super.getFactory(type);
    }
}
