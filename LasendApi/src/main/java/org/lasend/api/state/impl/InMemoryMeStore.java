package org.lasend.api.state.impl;

import org.lasend.api.model.Me;
import org.lasend.api.state.MeStore;

public class InMemoryMeStore implements MeStore {
    private Me me;

    //Todo synchronized
    @Override
    public void storeMe(Me me) {
        this.me = me;
    }

    @Override
    public Me getMe() {
        synchronized (me) {
            return me;
        }
    }
}
