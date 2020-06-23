package org.lasend.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class DeviceInfoResponse extends ResponseBase implements ResponseData {
    @Getter
    private String name;

    public DeviceInfoResponse(String name, String senderIdentity){
        super(senderIdentity);
        this.name = name;
    }
}
