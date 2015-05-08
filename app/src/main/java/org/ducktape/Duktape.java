package org.ducktape;

/**
 * Created by gcampagn on 5/8/15.
 */
public class Duktape {
    private long ctx;

    public Duktape() {
        ctx = createContext();
    }

    public void finalize() throws Throwable {
        destroyContext(ctx);
        super.finalize();
    }

    private native long createContext();
    private native long destroyContext(long ctx);

    static {
        System.loadLibrary("Duktape");
    }
}
