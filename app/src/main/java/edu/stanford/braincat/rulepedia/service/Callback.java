package edu.stanford.braincat.rulepedia.service;

import android.os.Handler;

/**
 * Created by gcampagn on 5/10/15.
 */
public abstract class Callback<T> {
    private final Handler handler;

    public Callback() {
        handler = new Handler();
    }

    public abstract void run(T result, Exception error);

    public void post(final T result, final Exception error) {
        handler.post(new Runnable() {
            public void run() {
                Callback.this.run(result, error);
            }
        });
    }
}
