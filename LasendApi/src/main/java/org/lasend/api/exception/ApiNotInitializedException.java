package org.lasend.api.exception;

public class ApiNotInitializedException extends Exception {
    public ApiNotInitializedException() {
        super("Api is not initialized");
    }
}
