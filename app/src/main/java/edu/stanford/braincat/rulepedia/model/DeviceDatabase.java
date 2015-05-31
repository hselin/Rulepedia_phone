package edu.stanford.braincat.rulepedia.model;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import edu.stanford.braincat.rulepedia.channels.ibeacon.IBeaconDevice;

/**
 * Created by braincat on 5/30/15.
 */
public class DeviceDatabase {
    private final Map<String, IBeaconDevice> devices;

    private final static DeviceDatabase instance = new DeviceDatabase();

    private DeviceDatabase() {
        devices = new HashMap<>();
    }

    public static DeviceDatabase get() {
        return instance;
    }

    public boolean addDevice(IBeaconDevice ibd) {
        if(devices.containsKey(ibd.uuid)) {
            return false;
        }

        devices.put(ibd.uuid, ibd);
        
        return true;
    }
}
