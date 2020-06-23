package org.lasend.api.dto.encrypted;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.lasend.api.util.EncryptionUtil;
import org.whispersystems.libsignal.SessionCipher;
import org.whispersystems.libsignal.UntrustedIdentityException;

@AllArgsConstructor
public class FileRequestDto extends EncryptedDtoBase implements EncryptedDto {

    public FileRequestDto(String fileId, SessionCipher sessionCipher, String senderIdentity) throws UntrustedIdentityException {
        super(EncryptionUtil.encryptPayload(new FileRequestDecryptedPayload(fileId), sessionCipher), senderIdentity);
    }

    public FileRequestDto(FileRequestDecryptedPayload payload, SessionCipher sessionCipher, String senderIdentity) throws UntrustedIdentityException {
        super(EncryptionUtil.encryptPayload(payload, sessionCipher), senderIdentity);
    }

    @AllArgsConstructor
    @NoArgsConstructor
    public static class FileRequestDecryptedPayload extends DecryptedPayload {
        @Getter
        private String fileId;
    }
}
