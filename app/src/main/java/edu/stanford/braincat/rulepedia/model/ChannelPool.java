package edu.stanford.braincat.rulepedia.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import edu.stanford.braincat.rulepedia.channels.HTTPUtil;
import edu.stanford.braincat.rulepedia.channels.android.NotificationManagerChannelFactory;
import edu.stanford.braincat.rulepedia.channels.android.SMSChannelFactory;
import edu.stanford.braincat.rulepedia.channels.generic.GenericChannelFactory;
import edu.stanford.braincat.rulepedia.channels.googlefit.GoogleFitChannelFactory;
import edu.stanford.braincat.rulepedia.channels.omlet.OmletChannelFactory;

/**
 * Created by gcampagn on 5/9/15.
 */
public class ChannelPool extends ObjectPool<Channel, ChannelFactory> {
    public static final String KIND = "channel";
    public static final String PREDEFINED_PREFIX = ObjectPool.PREDEFINED_PREFIX + KIND + "/";
    public static final String PLACEHOLDER_PREFIX = ObjectPool.PLACEHOLDER_PREFIX + KIND + "/";

    public static final String LOG_TAG = "rulepedia.Channels";

    private static final ChannelPool instance = new ChannelPool();

    public static ChannelPool get() {
        return instance;
    }

    public ChannelPool() {
        super(KIND);

        //registerFactory(new TimerFactory());
        registerFactory(new SMSChannelFactory());
        registerFactory(new NotificationManagerChannelFactory());
        //registerFactory(new OMDBChannelFactory());
        registerFactory(new GoogleFitChannelFactory());
        registerFactory(new OmletChannelFactory());

        try {
            JSONArray jsonChannels = (JSONArray) HTTPUtil.getJSON("https://vast-hamlet-6003.herokuapp.com/db/channels.json").nextValue();

            for (int i = 0; i < jsonChannels.length(); i++) {
                JSONObject channel = jsonChannels.getJSONObject(i);

                String id = channel.getString("id");
                if (hasFactory(id))
                    continue;

                ChannelFactory factory = new GenericChannelFactory(channel);
                registerFactory(factory);
            }
        } catch(IOException|JSONException e) {
            Log.e(LOG_TAG, "Failed to retrieve channel list from web server: " + e.getMessage());
        }
    }


}
