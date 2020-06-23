package org.lasend.api.exception;

public class InvalidChatIdException extends Exception {
    public InvalidChatIdException(String chatId) {
        super("No chat with id " + chatId + " exists");
    }
}
