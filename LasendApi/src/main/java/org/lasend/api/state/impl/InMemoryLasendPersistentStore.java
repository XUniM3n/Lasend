package org.lasend.api.state.impl;

import lombok.Getter;
import org.lasend.api.LasendPersistentStore;
import org.lasend.api.model.*;
import org.lasend.api.state.MeStore;

public class InMemoryLasendPersistentStore implements LasendPersistentStore {
    @Getter
    private final MeStore meStore = new InMemoryMeStore();
    @Getter
    private final InMemoryStateDao<Chat> chatStore = new InMemoryStateDao<Chat>();
    @Getter
    private final InMemoryStateDao<Contact> contactStore = new InMemoryStateDao<Contact>();
    @Getter
    private final InMemoryStateDao<SharedFile> sharedFileStore = new InMemoryStateDao<SharedFile>();
}
