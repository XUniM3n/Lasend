package org.lasend.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
public abstract class DtoBase implements DataDto {
    @Getter
    @Setter
    protected String senderIdentity;
}
