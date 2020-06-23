package org.lasend.api.exception;

public class DeviceIsNotOnlineException extends Exception {
    public DeviceIsNotOnlineException(String deviceFingerprint) {
        super(deviceFingerprint);
    }

    public DeviceIsNotOnlineException() {

    }
}
