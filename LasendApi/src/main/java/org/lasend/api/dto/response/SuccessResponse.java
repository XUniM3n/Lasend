package org.lasend.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class SuccessResponse extends ResponseBase implements ResponseData {
    public SuccessResponse(String senderIdentity){
        super(senderIdentity);
    }
}
