package org.lasend.api.model.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.lasend.api.dto.encrypted.message.FileLinkMessageDto;

@AllArgsConstructor
@NoArgsConstructor
public class FileLinkMessage extends Message {
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


    public FileLinkMessage(String chatId, String messageId, String senderFingerprint, String fileId, String fileName, String fileHash, long fileSize, String encryptionKey, String iv) {
        super(chatId, messageId, senderFingerprint);
        this.fileId = fileId;
        this.fileName = fileName;
        this.fileHash = fileHash;
        this.fileSize = fileSize;
        this.encryptionKey = encryptionKey;
        this.iv = iv;
    }

    public FileLinkMessage(String chatId, FileLinkMessageDto dto, FileLinkMessageDto.FileLinkDecryptedPayload payload) {
        super(chatId, payload.getMessageId(), dto.getSenderIdentity());
        this.fileId = payload.getFileId();
        this.fileName = payload.getFileName();
        this.fileHash = payload.getFileHash();
        this.fileSize = payload.getFileSize();
        this.encryptionKey = payload.getEncryptionKey();
        this.iv = payload.getIv();
    }

    public FileLinkMessage(String chatId, String senderIdentity, FileLinkMessageDto.FileLinkDecryptedPayload payload) {
        super(chatId, senderIdentity, payload.getMessageId());
        this.fileId = payload.getFileId();
        this.fileName = payload.getFileName();
        this.fileHash = payload.getFileHash();
        this.fileSize = payload.getFileSize();
        this.encryptionKey = payload.getEncryptionKey();
        this.iv = payload.getIv();
    }
}
