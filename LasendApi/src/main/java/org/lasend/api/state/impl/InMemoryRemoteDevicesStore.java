package org.lasend.api.state.impl;

import org.lasend.api.model.Device;

import java.net.InetAddress;

public class InMemoryRemoteDevicesStore extends InMemoryStateDao<Device> {
    public boolean contains(InetAddress address) {
        for (Device device : list) {
            if (device.getAddress().equals(address)) {
                return true;
            }
        }

        return false;
    }
}
