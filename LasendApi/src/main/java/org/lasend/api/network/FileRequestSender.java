package org.lasend.api.network;

import org.lasend.api.exception.*;
import org.lasend.api.model.chat.FileLinkMessage;
import org.whispersystems.libsignal.UntrustedIdentityException;

import java.io.File;
import java.io.IOException;

public interface FileRequestSender {
    void receiveFile(FileLinkMessage fileLink, File file) throws InvalidChatIdException, InvalidMessageTypeException, MessageNotFoundException, IOException, InvalidResponseException, InvalidFileReceivedException, UntrustedIdentityException, InvalidSharedFileLinkException;
}
