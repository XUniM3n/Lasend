package org.lasend.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.lasend.api.model.ContactRequestSent;
import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.ecc.Curve;
import org.whispersystems.libsignal.ecc.ECPublicKey;
import org.whispersystems.libsignal.state.PreKeyBundle;

@AllArgsConstructor
@NoArgsConstructor
public class ContactRequestDto extends DtoBase implements DataDto {
    @Getter
    private String senderName;
    @Getter
    private byte[] identityKey;
    @Getter
    private int deviceId;
    @Getter
    private byte[] preKey;
    @Getter
    private int preKeyId;
    @Getter
    private int registrationId;
    @Getter
    private byte[] signedPreKey;
    @Getter
    private int signedPreKeyId;
    @Getter
    private byte[] signedPreKeySignature;

    public ContactRequestDto(ContactRequestSent contactRequest, String senderIdentity, String senderName) {
        this(contactRequest.getPreKeyBundle(), senderIdentity, senderName);

    }

    public ContactRequestDto(PreKeyBundle preKeyBundle, String senderIdentity, String senderName) {
        super(senderIdentity);
        this.identityKey = preKeyBundle.getIdentityKey().serialize();
        this.deviceId = preKeyBundle.getDeviceId();
        this.preKey = preKeyBundle.getPreKey().serialize();
        this.preKeyId = preKeyBundle.getPreKeyId();
        this.registrationId = preKeyBundle.getRegistrationId();
        this.signedPreKey = preKeyBundle.getSignedPreKey().serialize();
        this.signedPreKeyId = preKeyBundle.getSignedPreKeyId();
        this.signedPreKeySignature = preKeyBundle.getSignedPreKeySignature();
        this.senderName = senderName;
    }

    public PreKeyBundle retrievePreKeyBundle() throws InvalidKeyException {
        ECPublicKey preKeyPublic = Curve.decodePoint(this.preKey, 0);
        ECPublicKey signedPreKeyPublic = Curve.decodePoint(this.signedPreKey, 0);
        return new PreKeyBundle(registrationId, deviceId, preKeyId, preKeyPublic, signedPreKeyId, signedPreKeyPublic, signedPreKeySignature, new IdentityKey(identityKey, 0));
    }
}
