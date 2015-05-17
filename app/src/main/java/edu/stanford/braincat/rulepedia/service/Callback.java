package edu.stanford.braincat.rulepedia.service;

import android.os.Handler;
import android.support.annotation.Nullable;

/**
 * Created by gcampagn on 5/10/15.
 */
public abstract class Callback<T> {
    private final Handler handler;

    public Callback() {
        handler = new Handler();
    }

    public abstract void run(@Nullable T result, @Nullable Exception error);

    public void post(final @Nullable T result, final @Nullable Exception error) {
        handler.post(new Runnable() {
            public void run() {
                Callback.this.run(result, error);
            }
        });
    }
}
