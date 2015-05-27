package edu.stanford.braincat.rulepedia.channels.omlet;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Created by gcampagn on 5/26/15.
 */
public class OmletMessage {
    private final long objectId;
    private final String objectType;
    private final long feedId;
    private JSONObject json;

    private OmletMessage(long objectId, String objectType, long feedId) {
        this.objectId = objectId;
        this.objectType = objectType;
        this.feedId = feedId;
    }

    public String getType() {
        return objectType;
    }

    private void ensureJSON(Context ctx) throws JSONException {
        if (json != null)
            return;

        try (Cursor queryCursor = ctx.getContentResolver().query(Uri.parse(OmletChannel.FEED_CONTENT_URI + feedId),
                new String[]{"json"},
                "id = ? and type = ?",
                new String[]{String.valueOf(feedId), objectType}, null)) {

            if (!queryCursor.moveToFirst())
                return;

            json = (JSONObject) new JSONTokener(queryCursor.getString(0)).nextValue();
        }
    }

    @Nullable
    public String getMessageText(Context ctx) {
        if (!objectType.equals("text"))
            return null;

        try {
            ensureJSON(ctx);
            if (json == null)
                return null;

            return json.getString("text");
        } catch(JSONException e) {
            return null;
        }
    }

    @Nullable
    public String getMessagePictureUri(Context ctx) {
        if (!objectType.equals("picture"))
            return null;

        try {
            ensureJSON(ctx);
            if (json == null)
                return null;

            return json.getString("imageUrl");
        } catch(JSONException e) {
            return null;
        }
    }

    public static OmletMessage fromBundle(Bundle bundle) {
        return new OmletMessage(bundle.getLong("mobisocial.intent.extra.OBJECT_ID"),
                bundle.getString("mobisocial.intent.extra.OBJECT_TYPE"),
                bundle.getLong("mobisocial.intent.extra.FEED_ID"));
    }
}
