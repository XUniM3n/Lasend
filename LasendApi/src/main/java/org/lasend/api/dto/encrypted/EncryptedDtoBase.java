package org.lasend.api.dto.encrypted;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.lasend.api.dto.DtoBase;

@AllArgsConstructor
@NoArgsConstructor
public abstract class EncryptedDtoBase extends DtoBase implements EncryptedDto {
    @Getter
    protected byte[] payload;

    public EncryptedDtoBase(byte[] payload, String senderIdentity) {
        this.payload = payload;
        this.senderIdentity = senderIdentity;
    }
}
