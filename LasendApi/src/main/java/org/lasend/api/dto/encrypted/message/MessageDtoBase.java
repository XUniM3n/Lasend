package org.lasend.api.dto.encrypted.message;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.lasend.api.dto.encrypted.EncryptedDtoBase;

@NoArgsConstructor
public class MessageDtoBase extends EncryptedDtoBase implements MessageDto {

    public MessageDtoBase(byte[] payload, String senderIdentity, String chatId) {
        super(payload, senderIdentity);
    }
}
