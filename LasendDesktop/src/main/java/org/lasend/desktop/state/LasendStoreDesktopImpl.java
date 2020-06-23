package org.lasend.desktop.state;

import lombok.Getter;
import org.lasend.api.LasendPersistentStore;
import org.lasend.api.model.*;
import org.lasend.api.state.MeStore;
import org.lasend.api.state.StateDao;

public class LasendStoreDesktopImpl implements LasendPersistentStore {
    @Getter
    private final MeStore meStore = new MeStoreDesktopImpl();

    @Override
    public StateDao<Chat> getChatStore() {
        return null;
    }

    @Override
    public StateDao<Contact> getContactStore() {
        return null;
    }

    @Override
    public StateDao<SharedFile> getSharedFileStore() {
        return null;
    }
}
