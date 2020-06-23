package org.lasend.api.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.whispersystems.libsignal.InvalidMessageException;
import org.whispersystems.libsignal.InvalidVersionException;
import org.whispersystems.libsignal.SessionCipher;
import org.whispersystems.libsignal.UntrustedIdentityException;
import org.whispersystems.libsignal.protocol.CiphertextMessage;
import org.whispersystems.libsignal.protocol.PreKeySignalMessage;

import java.nio.charset.StandardCharsets;

@AllArgsConstructor
@NoArgsConstructor
public class ContactAcceptDto extends DtoBase implements DataDto {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    @Getter
    private byte[] payload;

    public ContactAcceptDto(byte[] payload, String sender) {
        super(sender);
        this.payload = payload;
    }

    public ContactAcceptDto(String chatId, String senderIdentity, SessionCipher sessionCipher) throws UntrustedIdentityException {
        super(senderIdentity);
        this.payload = encryptPayload(chatId, sessionCipher);
    }

    public ContactAcceptDto(ContactAcceptDecryptedPayload payload, String senderIdentity, SessionCipher sessionCipher) throws UntrustedIdentityException {
        super(senderIdentity);
        this.payload = encryptPayload(payload, sessionCipher);
    }

    private static byte[] encryptPayload(ContactAcceptDecryptedPayload payload, SessionCipher sessionCipher) throws UntrustedIdentityException {
        String json = null;
        try {
            json = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        CiphertextMessage ciphertext = sessionCipher.encrypt(json.getBytes(StandardCharsets.UTF_8));
        return ciphertext.serialize();
    }

    private static byte[] encryptPayload(String chatId, SessionCipher sessionCipher) throws UntrustedIdentityException {
        ContactAcceptDecryptedPayload decryptedPayload = new ContactAcceptDecryptedPayload(chatId);
        return encryptPayload(decryptedPayload, sessionCipher);
    }

    @AllArgsConstructor
    @NoArgsConstructor
    public static class ContactAcceptDecryptedPayload {
        @Getter
        private String chatId;
    }
}
