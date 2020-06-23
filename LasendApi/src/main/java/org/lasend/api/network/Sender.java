package org.lasend.api.network;

import org.lasend.api.dto.*;
import org.lasend.api.dto.encrypted.FileRequestDto;
import org.lasend.api.dto.encrypted.message.MessageDecryptedPayload;
import org.lasend.api.dto.encrypted.message.MessageDto;
import org.lasend.api.model.Device;

public interface Sender {
    interface SenderCallback {
        void onChatMessageSent(MessageDto dto, MessageDecryptedPayload payload);

        void onContactRequestSent(ContactRequestDto dto);

        void onContactAcceptSent(ContactAcceptDto dto, ContactAcceptDto.ContactAcceptDecryptedPayload payload);

        void onContactRejectSent(ContactRejectDto dto);

        void onFileRequestSent(FileRequestDto dto, FileRequestDto.FileRequestDecryptedPayload payload);

        void onDeviceInfo(Device returnedDevice);
    }
}
