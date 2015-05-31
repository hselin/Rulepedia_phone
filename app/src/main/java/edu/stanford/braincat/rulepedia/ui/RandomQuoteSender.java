package edu.stanford.braincat.rulepedia.ui;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import edu.stanford.braincat.rulepedia.omletUI.OmletUIService;
import edu.stanford.braincat.rulepedia.omletUI.RandomQuotes;

/**
 * Created by gcampagn on 5/30/15.
 */
public class RandomQuoteSender extends TimerTask {
    private Activity parentActivity;
    private Timer autoUpdate;

    public RandomQuoteSender(Activity parent) {
        parentActivity = parent;
    }

    public void start() {
    }

    public void stop() {}

    public void resume() {
        autoUpdate = new Timer();
        autoUpdate.schedule(this, 0, 120000);
    }

    public void pause() {
        autoUpdate.cancel();
        autoUpdate = null;
    }

    @Override
    public void run() {
        parentActivity.runOnUiThread(new Runnable() {
            public void run() {
                Intent intent = new Intent(parentActivity, OmletUIService.class);
                intent.setAction(OmletUIService.SAY_RANDOM_QUOTES);
                intent.putExtra("QUOTE", RandomQuotes.getQuote());
                parentActivity.startService(intent);
                Log.d(MainActivity.LOG_TAG, "QUOTE!");
            }
        });
    }
}
