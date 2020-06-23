package org.lasend.api.dto.encrypted;

import org.lasend.api.dto.DataDto;

public interface EncryptedDto extends DataDto {
    byte[] getPayload();
}
