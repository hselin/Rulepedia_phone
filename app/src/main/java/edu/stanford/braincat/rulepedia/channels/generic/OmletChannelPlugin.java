package edu.stanford.braincat.rulepedia.channels.generic;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import org.mozilla.javascript.ScriptableObject;

/**
 * Created by gcampagn on 6/1/15.
 */
public class OmletChannelPlugin extends GenericChannelPlugin {
    public OmletChannelPlugin() {
        super(createIntent());
    }

    private static Intent createIntent() {
        Intent intent = new Intent("mobisocial.intent.action.BIND_SERVICE");
        intent.setPackage("mobisocial.omlet");
        return intent;
    }

    public void update(Context ctx, ScriptableObject object) {
        ContentResolver resolver = ctx.getContentResolver();
        try (Cursor cursor = resolver.query(Uri.parse("content://mobisocial.osm/identities"), new String[]{"principal"},
                "owned = 1 and hasApp = 1 and principal like 'omlet:%'", null, null)) {
            if (cursor == null) {
                Log.e("rulepedia.Games", "Can't get cursor to identities list");
                return;
            }

            if (!cursor.moveToFirst()) {
                Log.e("rulepedia.Games", "Can't find Omlet owner in identities list");
                return;
            }

            ScriptableObject.putProperty(object, "omlet_id", cursor.getString(0));
        }
    }
}
