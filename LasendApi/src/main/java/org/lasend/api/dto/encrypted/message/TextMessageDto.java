package org.lasend.api.dto.encrypted.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.lasend.api.util.EncryptionUtil;
import org.whispersystems.libsignal.SessionCipher;
import org.whispersystems.libsignal.UntrustedIdentityException;

@AllArgsConstructor
public class TextMessageDto extends MessageDtoBase implements MessageDto {

    public TextMessageDto(byte[] payload, String senderIdentity, String chatId) {
        super(payload, senderIdentity, chatId);
    }

    public TextMessageDto(String text, SessionCipher sessionCipher, String chatId, String messageId, String senderIdentity) throws UntrustedIdentityException {
        super(encryptPayload(text, sessionCipher, chatId, messageId), senderIdentity, chatId);
    }

    public TextMessageDto(TextDecryptedPayload payload, SessionCipher sessionCipher, String senderIdentity) throws UntrustedIdentityException {
        super(EncryptionUtil.encryptPayload(payload, sessionCipher), senderIdentity, payload.getChatId());
    }

    private static byte[] encryptPayload(String text, SessionCipher sessionCipher, String chatId, String messageId) throws UntrustedIdentityException {
        TextDecryptedPayload decryptedPayload = new TextDecryptedPayload(text, chatId, messageId);
        return EncryptionUtil.encryptPayload(decryptedPayload, sessionCipher);
    }

    @AllArgsConstructor
    @NoArgsConstructor
    public static class TextDecryptedPayload extends MessageDecryptedPayload {
        @Getter
        private String text;

        public TextDecryptedPayload(String text, String chatId, String messageId) {
            super(chatId, messageId);
            this.text = text;
        }
    }
}
