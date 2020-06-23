package org.lasend.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ContactRejectDto extends DtoBase implements DataDto {
    public ContactRejectDto(String senderIdentity){
        super(senderIdentity);
    }
}
