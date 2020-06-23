package org.lasend.api.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class DeviceInfoRequestDto extends DtoBase implements DataDto {
    @Getter
    private String name;

    public DeviceInfoRequestDto(String name, String senderIdentity) {
        super(senderIdentity);
        this.name = name;
    }
}
