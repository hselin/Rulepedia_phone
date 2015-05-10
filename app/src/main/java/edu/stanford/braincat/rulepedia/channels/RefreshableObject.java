package edu.stanford.braincat.rulepedia.channels;

import java.io.IOException;

/**
 * Created by gcampagn on 5/1/15.
 */
public interface RefreshableObject {
    void refresh() throws IOException;
}
