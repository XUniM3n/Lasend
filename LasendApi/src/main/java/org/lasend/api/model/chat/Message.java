package org.lasend.api.model.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public abstract class Message {
    @Getter
    private String chatId;
    @Getter
    private String messageId;
    @Getter
    private String senderIdentity;

    public Message(String senderIdentity){
        this.senderIdentity = senderIdentity;
    }
}
