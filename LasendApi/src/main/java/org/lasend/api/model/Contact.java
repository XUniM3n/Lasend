package org.lasend.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lasend.api.constant.EncryptionConstants;
import org.lasend.api.util.EncryptionUtil;
import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.SignalProtocolAddress;

@AllArgsConstructor
@NoArgsConstructor
public class Contact implements GetIdAble {
    @Getter
    private String id;
    @Getter
    private String name;
    @Getter
    private String identityKey;
    @Getter
    @Setter
    private String chatId;

    public Contact(String id, String name, IdentityKey identityPublicKey, String chatId) {
        this.id = id;
        this.name = name;
        this.identityKey = EncryptionUtil.identityPublicKeyToString(identityPublicKey);
        this.chatId = chatId;
    }

    public Contact(String id, String name, IdentityKey identityPublicKey) {
        this(id, name, identityPublicKey, null);
    }

    public SignalProtocolAddress getSignalProtocolAddress() {
        return new SignalProtocolAddress(identityKey, EncryptionConstants.DEVICE_ID);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Contact)) return false;
        return this.getId().equals(((Contact) obj).getId());
    }
}
