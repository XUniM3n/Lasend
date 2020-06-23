package org.lasend.api.exception;

public class ConnectionClosedException extends Exception {
    public ConnectionClosedException() {
        super("Connection with remote device closed");
    }
}
