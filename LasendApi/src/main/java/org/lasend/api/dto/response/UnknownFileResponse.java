package org.lasend.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class UnknownFileResponse extends ResponseBase implements ResponseData {
    public UnknownFileResponse(String senderIdentity){
        super(senderIdentity);
    }
}
