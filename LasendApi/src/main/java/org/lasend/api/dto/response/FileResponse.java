package org.lasend.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class FileResponse extends ResponseBase implements ResponseData {
    @Getter
    private String fileId;

    public FileResponse(String fileId, String senderIdentity){
        super(senderIdentity);
        this.fileId = fileId;
    }
}
