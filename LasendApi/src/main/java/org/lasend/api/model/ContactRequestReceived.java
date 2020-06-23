package org.lasend.api.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.whispersystems.libsignal.state.PreKeyBundle;

@AllArgsConstructor
@NoArgsConstructor
public class ContactRequestReceived implements GetIdAble {
    @Getter
    private String id;

    @Getter
    private String senderIdentity;

    @Getter
    private PreKeyBundle preKeyBundle;
}