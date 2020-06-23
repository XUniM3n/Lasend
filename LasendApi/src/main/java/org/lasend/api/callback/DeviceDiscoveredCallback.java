package org.lasend.api.callback;

import org.lasend.api.model.Device;

import java.net.InetAddress;

public interface DeviceDiscoveredCallback {
    void onDeviceDiscovered(Device device);
}
