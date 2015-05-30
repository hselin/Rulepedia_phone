package edu.stanford.braincat.rulepedia.channels.ibeacon;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;
import edu.stanford.braincat.rulepedia.model.Device;
import edu.stanford.braincat.rulepedia.model.DeviceFactory;
import edu.stanford.braincat.rulepedia.model.DevicePool;
import edu.stanford.braincat.rulepedia.model.PlaceholderDevice;

/**
 * Created by gcampagn on 5/30/15.
 */
public class IBeaconDeviceFactory extends DeviceFactory {
    public static final String ID = "ibeacon";
    public static final String URL_PREFIX = DevicePool.PREFIX + "/" + ID + "/";

    private Pattern urlPattern;

    public IBeaconDeviceFactory() {
        super(URL_PREFIX);

        urlPattern = Pattern.compile("https://rulepedia\\.stanford\\.edu/oid/devices/ibeacon/([a-z0-9]+)/([0-9])+/([0-9])+/([\\-[0-9]]+)");
    }

    public static IBeaconDeviceFactory getDefault() {
        return (IBeaconDeviceFactory) DevicePool.get().getFactory(ID);
    }

    @Override
    public IBeaconDevice create(String url) throws UnknownObjectException {
        Matcher m = urlPattern.matcher(url);
        if (!m.matches())
            throw new UnknownObjectException(url);

        try {
            return new IBeaconDevice(this, m.group(1),
                    Integer.parseInt(m.group(2), 10), Integer.parseInt(m.group(3), 10), Integer.parseInt(m.group(4), 10));
        } catch(NumberFormatException e) {
            throw new UnknownObjectException(url);
        }
    }

    @Override
    public Device createPlaceholder(String url) {
        return new PlaceholderDevice(this, url, "an ibeacon");
    }

    @Override
    public String getName() {
        return ID;
    }
}
