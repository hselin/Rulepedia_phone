package edu.stanford.braincat.rulepedia.channels.ibeacon;

import java.util.Arrays;

import edu.stanford.braincat.rulepedia.model.Device;
import edu.stanford.braincat.rulepedia.model.DeviceFactory;

/**
 * Created by braincat on 5/29/15.
 */
public class IBeaconDevice extends Device {
    public final String uuid;
    public final int major;
    public final int minor;
    public Number tx;

    public String deviceType;

    public IBeaconDevice(DeviceFactory factory, String uuid, int major, int minor, Number tx) {
        super(factory, IBeaconDeviceFactory.URL_PREFIX + uuid + "/" + major + "/" + minor + "/" + tx);

        this.uuid = uuid;
        this.major = major;
        this.minor = minor;
        this.tx = tx;

        this.deviceType = parseDeviceType();
    }

    public String toHumanString() {
        return parseDeviceType();
    }

    private String parseDeviceType() {
        switch(major) {
            case 1: return "Corktastic";
        }

        return "Unknown";
    }


    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static String bytesToHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static IBeaconDevice newIBeaconDevice(final byte[] scanRecord) {
        try {
            String prefix = bytesToHexString(Arrays.copyOf(scanRecord, 9));

            if (!prefix.equals("02011A1AFF4C000215"))
                return null;

            String uuid = bytesToHexString(Arrays.copyOfRange(scanRecord, 9, 25));
            int major = Integer.parseInt(bytesToHexString(Arrays.copyOfRange(scanRecord, 25, 27)), 16);
            int minor = Integer.parseInt(bytesToHexString(Arrays.copyOfRange(scanRecord, 27, 29)), 16);
            int tx = Integer.parseInt(bytesToHexString(Arrays.copyOfRange(scanRecord, 29, 30)), 16);

            return new IBeaconDevice(IBeaconDeviceFactory.getDefault(), uuid, major, minor, tx);
        } catch(NumberFormatException e) {
            return null;
        }
    }

}
