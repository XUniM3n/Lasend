package org.lasend.api.exception;

public class InvalidSharedFileLinkException extends Exception {
    public InvalidSharedFileLinkException(String fileId){
        super("Invalid File Link " + fileId);
    }
}
