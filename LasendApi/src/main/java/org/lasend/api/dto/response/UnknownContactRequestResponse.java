package org.lasend.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class UnknownContactRequestResponse extends ResponseBase implements ResponseData {
    public UnknownContactRequestResponse(String senderIdentity){
        super(senderIdentity);
    }
}
