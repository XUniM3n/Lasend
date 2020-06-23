package org.lasend.api.network;

import org.lasend.api.model.SharedFile;
import org.lasend.api.model.chat.FileLinkMessage;
import org.lasend.api.model.chat.TextMessage;
import org.whispersystems.libsignal.UntrustedIdentityException;

import java.io.IOException;

public interface MessageSender extends Sender {
    TextMessage sendTextMessage(String text, String chatId) throws IOException, UntrustedIdentityException;

    FileLinkMessage sendFileLinkMessage(SharedFile sharedFile, String chatId) throws IOException, UntrustedIdentityException;
}
