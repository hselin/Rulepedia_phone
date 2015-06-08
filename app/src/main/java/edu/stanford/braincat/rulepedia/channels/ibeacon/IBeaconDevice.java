package edu.stanford.braincat.rulepedia.channels.ibeacon;

import edu.stanford.braincat.rulepedia.channels.Util;
import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;
import edu.stanford.braincat.rulepedia.model.Device;

/**
 * Created by braincat on 5/29/15.
 */
public class IBeaconDevice extends Device {
    private static final int CORKTASTIC_MAJOR = 1;
    private static final int DOORBELL_MAJOR = 2;
    private static final int ANDROID_WEAR_DEVICE_MAJOR = 3;

    public final String uuid;
    public final int major;
    public final int minor;
    public final String deviceType;

    public IBeaconDevice(IBeaconDeviceFactory factory, String uuid, int major, int minor) throws UnknownObjectException {
        super(factory, IBeaconDeviceFactory.URL_PREFIX + uuid + "/" + major + "/" + minor);

        this.uuid = uuid;
        this.major = major;
        this.minor = minor;

        switch(major) {
            case CORKTASTIC_MAJOR:
                deviceType = "corktastic";
                break;

            case DOORBELL_MAJOR:
                deviceType = "doorbell";
                break;

            case ANDROID_WEAR_DEVICE_MAJOR:
                deviceType = "google";
                break;

            default:
                deviceType = "unknown";
                //throw new UnknownObjectException(getUrl());
        }
    }

    public String toHumanString() {
        switch (deviceType) {
            case "corktastic":
                return "Corktastic";

            case "google":
                return "Android Wear";

            case "doorbell":
                return "iDoorbell";

            default:
                return deviceType;
        }
    }

    public static IBeaconDevice newIBeaconDevice(final byte[] scanRecord) {
        try {
            String prefix = Util.bytesToHexString(scanRecord, 0, 9);

            if (!prefix.equals("02011A1AFF4C000215"))
                return null;

            String uuid = Util.bytesToHexString(scanRecord, 9, 25);

            int major = ((int)scanRecord[25] << 8) + ((int)scanRecord[26]);
            int minor = ((int)scanRecord[27] << 8) + ((int)scanRecord[28]);

            return new IBeaconDevice(IBeaconDeviceFactory.getDefault(), uuid, major, minor);
        } catch(NumberFormatException|UnknownObjectException e) {
            return null;
        }
    }

}
