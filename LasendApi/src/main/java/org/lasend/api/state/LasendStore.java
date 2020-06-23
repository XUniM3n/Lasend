package org.lasend.api.state;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.lasend.api.LasendPersistentStore;

@AllArgsConstructor
public class LasendStore {
    @Getter
    private final LasendPersistentStore persistent;
    @Getter
    private final LasendTempStore temp;
}
