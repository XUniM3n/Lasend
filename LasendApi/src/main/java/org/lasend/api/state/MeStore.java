package org.lasend.api.state;

import org.lasend.api.model.Me;

public interface MeStore {
    void storeMe(Me me);

    Me getMe();
}
