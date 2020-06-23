package org.lasend.api.network;

import org.lasend.api.model.Device;

import java.io.IOException;

public interface DeviceInfoGetter extends Sender {
    Device getDeviceInfo() throws IOException;
}
