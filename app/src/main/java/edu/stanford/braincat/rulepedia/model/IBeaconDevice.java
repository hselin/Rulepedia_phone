package edu.stanford.braincat.rulepedia.model;

import android.util.Log;

import java.util.Arrays;

/**
 * Created by braincat on 5/29/15.
 */
public class IBeaconDevice {
    public String uuid;
    public String major;
    public String minor;
    public String tx;

    public String deviceType;

    public IBeaconDevice(String uuid, String major, String minor, String tx){
        this.uuid = uuid;
        this.major = major;
        this.minor = minor;
        this.tx = tx;

        this.deviceType = parseDeviceType();
    }

    private String parseDeviceType()
    {
        int major_number = Integer.parseInt(this.major, 16);

        switch(major_number)
        {
            case 1: return "TV";
        }

        return "Unknown Device";
    }


    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static IBeaconDevice newIBeaconDevice(final byte[] scanRecord) {
        //Log.d("!!!!!!!!", "bytesToHex: " + bytesToHexString(scanRecord));

        String prefix = bytesToHexString(Arrays.copyOf(scanRecord, 9));

        //Log.d("!!!!!!!!", "prefix: " + prefix);
        if (prefix.equals("02011A1AFF4C000215")) {
            String uuid = bytesToHexString(Arrays.copyOfRange(scanRecord, 9, 25));
            String major = bytesToHexString(Arrays.copyOfRange(scanRecord, 25, 27));
            String minor = bytesToHexString(Arrays.copyOfRange(scanRecord, 27, 29));
            String tx = bytesToHexString(Arrays.copyOfRange(scanRecord, 29, 30));

            Log.d("!!!!!!!!", "uuid: " + uuid);
            Log.d("!!!!!!!!", "major: " + major);
            Log.d("!!!!!!!!", "minor: " + minor);
            Log.d("!!!!!!!!", "tx: " + tx);

            return new IBeaconDevice(uuid, major, minor, tx);
        }


        return null;
    }

}
