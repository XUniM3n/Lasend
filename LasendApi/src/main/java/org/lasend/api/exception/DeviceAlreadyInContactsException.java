package org.lasend.api.exception;

import org.lasend.api.model.Device;

public class DeviceAlreadyInContactsException extends Exception {
    public DeviceAlreadyInContactsException(Device device) {
        super(device.getName() + " already in contacts");
    }
}
