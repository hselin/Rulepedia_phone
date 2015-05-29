package edu.stanford.braincat.rulepedia.channels.omlet;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * Created by gcampagn on 5/26/15.
 */
public class OmletMessage {
    private final long objectId;
    private final String objectType;
    private final long feedId;
    private String json;

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

    private void ensureJSON(Context ctx) {
        if (json != null)
            return;

        try (Cursor queryCursor = ctx.getContentResolver().query(Uri.parse(getFeedUri()),
                new String[]{"json"},
                "id = ? and type = ?",
                new String[]{String.valueOf(objectId), objectType}, null)) {

            if (!queryCursor.moveToFirst())
                return;

            json = queryCursor.getString(0);
        }
    }

    @Nullable
    public String getJSON(Context ctx) {
        ensureJSON(ctx);
        return json;
    }

    public static OmletMessage fromBundle(Bundle bundle) {
        return new OmletMessage(bundle.getLong("mobisocial.intent.extra.OBJECT_ID"),
                bundle.getString("mobisocial.intent.extra.OBJECT_TYPE"),
                bundle.getLong("mobisocial.intent.extra.FEED_ID"));
    }
}
