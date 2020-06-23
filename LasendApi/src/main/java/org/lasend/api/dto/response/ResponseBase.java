package org.lasend.api.dto.response;

import lombok.AllArgsConstructor;
import org.lasend.api.dto.DtoBase;

@AllArgsConstructor
public abstract class ResponseBase extends DtoBase {
    public ResponseBase(String senderIdentity) {
        super(senderIdentity);
    }
}
