package org.lasend.api.state;

import lombok.Getter;
import org.lasend.api.model.ContactOnline;
import org.lasend.api.state.impl.InMemoryRemoteDevicesStore;
import org.lasend.api.state.impl.InMemoryStateDao;

public class LasendTempStore {
    @Getter
    private InMemoryRemoteDevicesStore foundRemoteDevicesStore = new InMemoryRemoteDevicesStore();
    @Getter
    private StateDao<ContactOnline> foundOnlineContactsStore = new InMemoryStateDao<ContactOnline>();
}
