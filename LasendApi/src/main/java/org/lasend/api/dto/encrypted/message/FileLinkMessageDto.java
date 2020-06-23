package org.lasend.api.dto.encrypted.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.lasend.api.model.SharedFile;
import org.lasend.api.util.EncryptionUtil;
import org.whispersystems.libsignal.SessionCipher;
import org.whispersystems.libsignal.UntrustedIdentityException;

import java.util.UUID;

@AllArgsConstructor
public class FileLinkMessageDto extends MessageDtoBase implements MessageDto {
    public FileLinkMessageDto(byte[] payload, String senderIdentity, String chatId) {
        super(payload, senderIdentity, chatId);
    }

    public FileLinkMessageDto(SharedFile sharedFile, SessionCipher sessionCipher, String chatId, String messageId, String senderIdentity) throws UntrustedIdentityException {
        super(sharedFileToPayload(sharedFile, sessionCipher, chatId, messageId), senderIdentity, chatId);
    }

    public FileLinkMessageDto(FileLinkDecryptedPayload payload, SessionCipher sessionCipher, String senderIdentity) throws UntrustedIdentityException {
        super(EncryptionUtil.encryptPayload(payload, sessionCipher), senderIdentity, payload.getChatId());
    }

    private static byte[] sharedFileToPayload(SharedFile sharedFile, SessionCipher sessionCipher, String chatId, String messageId) throws UntrustedIdentityException {
        String fileId = UUID.randomUUID().toString();
        String fileName = sharedFile.getName();
        String fileHash = sharedFile.getHash();
        long fileSize = sharedFile.getSize();
        String encryptionKey = sharedFile.getEncryptionKey();
        String iv = sharedFile.getIv();
        FileLinkDecryptedPayload decryptedPayload = new FileLinkDecryptedPayload(fileId, fileName, fileHash, fileSize, encryptionKey, iv, chatId, messageId);
        return EncryptionUtil.encryptPayload(decryptedPayload, sessionCipher);
    }

    @AllArgsConstructor
    @NoArgsConstructor
    public static class FileLinkDecryptedPayload extends MessageDecryptedPayload {
        @Getter
        private String fileId;
        @Getter
        private String fileName;
        @Getter
        private String fileHash;
        @Getter
        private long fileSize;
        @Getter
        private String encryptionKey;
        @Getter
        private String iv;

        public FileLinkDecryptedPayload(String fileId, String fileName, String fileHash, long fileSize, String encryptionKey, String iv, String chatId, String messageId) {
            super(chatId, messageId);
            this.fileId = fileId;
            this.fileName = fileName;
            this.fileHash = fileHash;
            this.fileSize = fileSize;
            this.encryptionKey = encryptionKey;
            this.iv = iv;
        }

        public FileLinkDecryptedPayload(SharedFile sharedFile, String chatId, String messageId) {
            this(sharedFile.getId(), sharedFile.getName(), sharedFile.getHash(), sharedFile.getSize(), sharedFile.getEncryptionKey(), sharedFile.getIv(), chatId, messageId);
        }
    }
}
