package org.lasend.api.network;

import org.lasend.api.dto.ContactAcceptDto;
import org.lasend.api.dto.ContactRejectDto;
import org.lasend.api.dto.ContactRequestDto;
import org.lasend.api.dto.encrypted.message.MessageDecryptedPayload;
import org.lasend.api.dto.encrypted.message.MessageDto;
import org.lasend.api.dto.encrypted.message.TextMessageDto;
import org.lasend.api.dto.response.DeviceInfoResponse;
import org.lasend.api.model.Chat;
import org.lasend.api.model.ContactRequestReceived;
import org.lasend.api.model.Device;
import org.lasend.api.model.chat.Message;
import org.whispersystems.libsignal.fingerprint.DisplayableFingerprint;

public interface Receiver {
    boolean isListening();

    void start();

    void stop();

    interface ReceiverCallback {
        void onChatMessageReceived(Message message, Chat chat);

        void onContactRequestReceived(ContactRequestReceived contactRequest, Device sender, String comparableFingerprint);

        void onContactAcceptReceived(ContactAcceptDto dto, ContactAcceptDto.ContactAcceptDecryptedPayload payload, Device sender);

        void onContactRejectReceived(ContactRejectDto dto, Device sender);
    }
}
