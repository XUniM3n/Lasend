package org.lasend.api;

import org.lasend.api.model.*;
import org.lasend.api.state.MeStore;
import org.lasend.api.state.StateDao;

public interface LasendPersistentStore {
    MeStore getMeStore();

    StateDao<Chat> getChatStore();

    StateDao<Contact> getContactStore();

    StateDao<SharedFile> getSharedFileStore();
}
