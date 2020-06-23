package org.lasend.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.lasend.api.constant.EncryptionConstants;
import org.lasend.api.util.EncryptionUtil;
import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.InvalidKeyIdException;
import org.whispersystems.libsignal.state.PreKeyBundle;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.SignalProtocolStore;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;
import org.whispersystems.libsignal.state.impl.InMemorySignalProtocolStore;
import org.whispersystems.libsignal.util.KeyHelper;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
public class Me {
    @Getter
    private String name;
    @Getter
    private String identity;
    @Getter
    private SignalProtocolStore signalProtocolStore;
    @Getter
    private long registrationDate;
    private int preKeyCounter;
    private int signedPreKeyCounter;

    public Me(String name) {
        this.name = name;
        this.registrationDate = System.currentTimeMillis();

        IdentityKeyPair identityKeyPair = KeyHelper.generateIdentityKeyPair();
        int registrationId = KeyHelper.generateRegistrationId(false);
        this.signalProtocolStore = new InMemorySignalProtocolStore(identityKeyPair, registrationId);

        this.preKeyCounter = 0;
        List<PreKeyRecord> preKeys = KeyHelper.generatePreKeys(0, EncryptionConstants.PREKEY_COUNT);
        for (int i = 0; i < EncryptionConstants.PREKEY_COUNT; i++) {
            this.signalProtocolStore.storePreKey(i, preKeys.get(i));
        }

        this.signedPreKeyCounter = 0;
        SignedPreKeyRecord signedPreKey = null;
        try {
            signedPreKey = KeyHelper.generateSignedPreKey(identityKeyPair, signedPreKeyCounter);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        this.signalProtocolStore.storeSignedPreKey(signedPreKeyCounter, signedPreKey);
        this.identity = EncryptionUtil.identityPublicKeyToString(identityKeyPair.getPublicKey());
    }

    public Me(String name, SignalProtocolStore signalProtocolStore) {
        this.name = name;
        this.signalProtocolStore = signalProtocolStore;
        this.identity = EncryptionUtil.identityPublicKeyToString(signalProtocolStore.getIdentityKeyPair().getPublicKey());
    }

    public PreKeyBundle getPreKeyBundle() {
        PreKeyBundle preKeyBundle = null;
        try {
            preKeyBundle = new PreKeyBundle(signalProtocolStore.getLocalRegistrationId(), EncryptionConstants.DEVICE_ID,
                    preKeyCounter, retrievePreKey().getKeyPair().getPublicKey(), signedPreKeyCounter,
                    signalProtocolStore.loadSignedPreKey(signedPreKeyCounter).getKeyPair().getPublicKey(),
                    signalProtocolStore.loadSignedPreKey(signedPreKeyCounter).getSignature(),
                    signalProtocolStore.getIdentityKeyPair().getPublicKey());
        } catch (InvalidKeyIdException e) {
            e.printStackTrace();
        }

        return preKeyBundle;
    }

    public PreKeyRecord retrievePreKey() {
        PreKeyRecord preKey = null;
        try {
            preKey = signalProtocolStore.loadPreKey(preKeyCounter);
            signalProtocolStore.storePreKey(preKeyCounter + EncryptionConstants.PREKEY_COUNT, KeyHelper.generatePreKeys(preKeyCounter + EncryptionConstants.PREKEY_COUNT, 1).get(0));
            preKeyCounter++;
        } catch (InvalidKeyIdException e) {
            e.printStackTrace();
        }
        return preKey;
    }

    // Rotate every 48 hours
    public void rotateSignedPreKeyIfNeeded() {
        if (System.currentTimeMillis() > (registrationDate + signedPreKeyCounter * (48 * 60 * 60 * 1000))) {
            try {
                signalProtocolStore.removeSignedPreKey(signedPreKeyCounter);
                signedPreKeyCounter++;
                SignedPreKeyRecord signedPreKey = KeyHelper.generateSignedPreKey(signalProtocolStore.getIdentityKeyPair(), signedPreKeyCounter);
                signalProtocolStore.storeSignedPreKey(signedPreKeyCounter, signedPreKey);
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            }
        }
    }
}
