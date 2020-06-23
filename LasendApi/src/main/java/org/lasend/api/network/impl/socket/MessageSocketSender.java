package org.lasend.api.network.impl.socket;

import org.lasend.api.LasendCallbacks;
import org.lasend.api.constant.NetworkConstants;
import org.lasend.api.dto.encrypted.message.FileLinkMessageDto;
import org.lasend.api.dto.encrypted.message.MessageDto;
import org.lasend.api.dto.encrypted.message.TextMessageDto;
import org.lasend.api.dto.response.ResponseData;
import org.lasend.api.dto.response.SuccessResponse;
import org.lasend.api.model.ContactOnline;
import org.lasend.api.model.SharedFile;
import org.lasend.api.model.chat.FileLinkMessage;
import org.lasend.api.model.chat.TextMessage;
import org.lasend.api.network.MessageSender;
import org.lasend.api.service.SentDataProcessor;
import org.lasend.api.state.LasendStore;
import org.whispersystems.libsignal.SessionCipher;
import org.whispersystems.libsignal.UntrustedIdentityException;

import java.io.IOException;
import java.util.UUID;

public class MessageSocketSender extends TcpSocketSender implements MessageSender {
    private final ContactOnline contactOnline;
    private final SessionCipher sessionCipher;

    public MessageSocketSender(ContactOnline contactOnline, LasendStore store, LasendCallbacks callbacks) {
        super(contactOnline.getDevice().getAddress(), NetworkConstants.PORT_LISTEN, store, callbacks);
        this.contactOnline = contactOnline;
        this.sessionCipher = new SessionCipher(store.getPersistent().getMeStore().getMe().getSignalProtocolStore(), contactOnline.getDevice().getSignalProtocolAddress());
    }

    public TextMessage sendTextMessage(String text, String chatId) throws IOException, UntrustedIdentityException {
        TextMessageDto.TextDecryptedPayload payload = new TextMessageDto.TextDecryptedPayload(text, chatId, UUID.randomUUID().toString());
        TextMessageDto messageDto = new TextMessageDto(payload, sessionCipher, store.getPersistent().getMeStore().getMe().getIdentity());
        sendMessage(messageDto);
        return SentDataProcessor.processTextMessage(messageDto, payload, contactOnline.getDevice(), store, senderCallback, exceptionCallback);
    }

    public FileLinkMessage sendFileLinkMessage(SharedFile sharedFile, String chatId) throws IOException, UntrustedIdentityException {
        FileLinkMessageDto.FileLinkDecryptedPayload payload = new FileLinkMessageDto.FileLinkDecryptedPayload(sharedFile, chatId, UUID.randomUUID().toString());
        FileLinkMessageDto messageDto = new FileLinkMessageDto(payload, sessionCipher, store.getPersistent().getMeStore().getMe().getIdentity());
        sendMessage(messageDto);
        return SentDataProcessor.processFileLinkMessage(messageDto, payload, contactOnline.getDevice(), store, senderCallback, exceptionCallback);
    }

    private void sendMessage(MessageDto message) throws IOException {
        try {
            if (!isConnected) {
                connect();
            }

            sendObject(message);

            String line = reader.readLine();
            ResponseData response = requestObjectMapper.readValue(line, ResponseData.class);

            if (!(response instanceof SuccessResponse)) {
                throw new IOException();
            }

        } catch (IOException e) {
            stop();
            contactOnline.setMessageSender(null);
            exceptionCallback.onException(e, Thread.currentThread());
            throw e;
        }
    }
}
