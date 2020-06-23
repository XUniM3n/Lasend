package org.lasend.api.exception;

public class NoContactWithSuchDeviceException extends Exception {
    public NoContactWithSuchDeviceException() {
        super("No contact with such device");
    }
}
