package org.lasend.api.model;

import lombok.*;
import org.lasend.api.constant.EncryptionConstants;
import org.lasend.api.util.EncryptionUtil;
import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.SignalProtocolAddress;

import java.net.InetAddress;

@AllArgsConstructor
@Builder
@NoArgsConstructor
public class Device implements GetIdAble {
    @Getter
    private InetAddress address;
    @Getter
    private String identity;
    @Getter
    @Setter
    private String name;

    public Device(InetAddress address, String name, IdentityKey publicIdentityKey) {
        this.address = address;
        this.name = name;
        this.identity = EncryptionUtil.identityPublicKeyToString(publicIdentityKey);
    }

    public String getId() {
        return identity;
    }

    public SignalProtocolAddress getSignalProtocolAddress() {
        return new SignalProtocolAddress(identity, EncryptionConstants.DEVICE_ID);
    }

    public IdentityKey getIdentityKey() {
        return EncryptionUtil.stringToIdentityPublicKey(identity);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Device)) {
            return false;
        }

        return getId().equals(((Device) obj).getId());
    }
}
