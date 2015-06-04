package edu.stanford.braincat.rulepedia.channels.omlet;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import edu.stanford.braincat.rulepedia.exceptions.UnknownObjectException;
import edu.stanford.braincat.rulepedia.model.Contact;
import edu.stanford.braincat.rulepedia.model.ContactPool;

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

    public String getFeedUri() {
        return OmletChannel.FEED_CONTENT_URI + feedId;
    }

    public Contact getSender() throws UnknownObjectException {
        return ContactPool.get().getObject(getFeedUri());
    }

    private void ensureJSON(Context ctx) {
        if (json != null)
            return;

        try (Cursor queryCursor = ctx.getContentResolver().query(Uri.parse(getFeedUri()),
                new String[]{"json"},
                "Id = ? and type = ?",
                new String[]{String.valueOf(objectId), objectType}, null)) {

            if (!queryCursor.moveToFirst())
                return;

            try {
                String jsonString = queryCursor.getString(0);
                json = (JSONObject) new JSONTokener(jsonString).nextValue();
            } catch(JSONException | ClassCastException e) {
                json = null;
            }
        }
    }

    @Nullable
    public String getJSON(Context ctx) {
        ensureJSON(ctx);
        if (json != null)
            return json.toString();
        else
            return null;
    }

    @Nullable
    public String getText(Context ctx) {
        try {
            ensureJSON(ctx);
            if (json != null) {
                return json.getString("text");
            } else {
                return null;
            }
        } catch(JSONException e) {
            return null;
        }
    }

    @Nullable
    public String getPicture(Context ctx) {
        try {
            ensureJSON(ctx);
            if (json != null) {
                if (json.has("imageUrl"))
                    return json.getString("imageUrl");
                else
                    return "data:base64," + json.getString("imageData");
            } else {
                return null;
            }
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
