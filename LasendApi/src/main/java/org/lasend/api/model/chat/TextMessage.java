package org.lasend.api.model.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.lasend.api.dto.encrypted.message.TextMessageDto;

@AllArgsConstructor
@NoArgsConstructor
public class TextMessage extends Message {
    @Getter
    private String text;

    public TextMessage(String text, String chatId, String messageId, String senderFingerprint) {
        super(chatId, messageId, senderFingerprint);
        this.text = text;
    }

    public TextMessage(String chatId, TextMessageDto dto, TextMessageDto.TextDecryptedPayload payload) {
        super(chatId, payload.getMessageId(), dto.getSenderIdentity());
        this.text = payload.getText();
    }

    public TextMessage(String chatId, String senderIdentity, TextMessageDto.TextDecryptedPayload payload) {
        super(chatId, payload.getMessageId(), senderIdentity);
        this.text = payload.getText();
    }
}
