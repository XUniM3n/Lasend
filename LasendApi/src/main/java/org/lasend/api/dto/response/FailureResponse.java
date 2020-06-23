package org.lasend.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class FailureResponse extends ResponseBase implements ResponseData {
    public FailureResponse(String senderIdentity){
        super(senderIdentity);
    }
}
