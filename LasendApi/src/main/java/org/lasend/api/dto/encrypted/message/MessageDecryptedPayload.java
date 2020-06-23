package org.lasend.api.dto.encrypted.message;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.lasend.api.dto.encrypted.DecryptedPayload;

@AllArgsConstructor
@NoArgsConstructor
public abstract class MessageDecryptedPayload extends DecryptedPayload {
    @Getter
    protected String chatId;
    @Getter
    protected String messageId;
}
