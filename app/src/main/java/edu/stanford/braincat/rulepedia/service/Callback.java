package edu.stanford.braincat.rulepedia.service;

import android.os.Handler;

/**
 * Created by gcampagn on 5/10/15.
 */
public abstract class Callback {
    private final Handler handler;

    public Callback() {
        handler = new Handler();
    }

    public abstract void run(Exception e);

    public void post(final Exception e) {
        handler.post(new Runnable() {
            public void run() {
                Callback.this.run(e);
            }
        });
    }
}
