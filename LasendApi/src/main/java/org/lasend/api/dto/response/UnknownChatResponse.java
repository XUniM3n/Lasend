package org.lasend.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class UnknownChatResponse extends ResponseBase implements ResponseData {
    public UnknownChatResponse(String senderIdentity){
        super(senderIdentity);
    }
}
