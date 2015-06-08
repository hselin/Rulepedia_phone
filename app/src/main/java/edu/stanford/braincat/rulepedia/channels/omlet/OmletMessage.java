package edu.stanford.braincat.rulepedia.channels.omlet;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;

import edu.stanford.braincat.rulepedia.channels.Util;
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
    private String cachedJson;
    private String cachedText;
    private String cachedImageUrl;

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

    @Nullable
    public String getText(Context ctx) {
        if (cachedText != null)
            return cachedText;

        if (ctx == null)
            return null;

        try (Cursor queryCursor = ctx.getContentResolver().query(Uri.parse(OmletChannel.CONTENT_URI),
                new String[]{"text"},
                "Id = ?",
                new String[]{String.valueOf(objectId)}, null)) {
            if (!queryCursor.moveToFirst())
                return null;

            cachedText = queryCursor.getString(0);
            return cachedText;
        }
    }

    @Nullable
    public String getPicture(Context ctx) {
        if (cachedImageUrl != null)
            return cachedImageUrl;

        if (ctx == null)
            return null;

        try (Cursor queryCursor = ctx.getContentResolver().query(Uri.parse(OmletChannel.CONTENT_URI),
                new String[]{"fullsizeHash"},
                "Id = ?",
                new String[]{String.valueOf(objectId)}, null)) {
            if (!queryCursor.moveToFirst())
                return null;

            cachedImageUrl = "content://mobisocial.osm/blobs/" + Util.bytesToHexString(queryCursor.getBlob(0)).toLowerCase();
            return cachedImageUrl;
        }
    }

    public static OmletMessage fromBundle(Bundle bundle) {
        return new OmletMessage(bundle.getLong("mobisocial.intent.extra.OBJECT_ID"),
                bundle.getString("mobisocial.intent.extra.OBJECT_TYPE"),
                bundle.getLong("mobisocial.intent.extra.FEED_ID"));
    }
}
