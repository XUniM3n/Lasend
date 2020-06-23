package org.lasend.api.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.lasend.api.constant.EncryptionConstants;
import org.lasend.api.dto.ContactAcceptDto;
import org.lasend.api.dto.encrypted.DecryptedPayload;
import org.lasend.api.model.Device;
import org.lasend.api.state.LasendStore;
import org.whispersystems.libsignal.*;
import org.whispersystems.libsignal.fingerprint.DisplayableFingerprint;
import org.whispersystems.libsignal.fingerprint.Fingerprint;
import org.whispersystems.libsignal.fingerprint.FingerprintGenerator;
import org.whispersystems.libsignal.fingerprint.NumericFingerprintGenerator;
import org.whispersystems.libsignal.protocol.CiphertextMessage;
import org.whispersystems.libsignal.protocol.PreKeySignalMessage;
import org.whispersystems.libsignal.protocol.SignalMessage;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;

public class EncryptionUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ObjectMapper payloadObjectMapper = JsonMapper.builder()
            .activateDefaultTyping(BasicPolymorphicTypeValidator.builder()
                    .allowIfSubType(DecryptedPayload.class)
                    .build(), ObjectMapper.DefaultTyping.NON_FINAL).build();
    private final static Base64 base64 = new Base64();
    private static Cipher cipher;

    public static ContactAcceptDto.ContactAcceptDecryptedPayload decryptPreKeyPayload(byte[] encryptedPayload, SessionCipher sessionCipher) throws LegacyMessageException, InvalidMessageException, DuplicateMessageException, UntrustedIdentityException, JsonProcessingException, InvalidVersionException, InvalidKeyException, InvalidKeyIdException {
        PreKeySignalMessage preKeySignalMessage = new PreKeySignalMessage(encryptedPayload);
        byte[] decryptedPayloadBytes = sessionCipher.decrypt(preKeySignalMessage);
        String decryptedPayloadJson = new String(decryptedPayloadBytes, StandardCharsets.UTF_8);
        ContactAcceptDto.ContactAcceptDecryptedPayload decryptedPayload = objectMapper.readValue(decryptedPayloadJson, ContactAcceptDto.ContactAcceptDecryptedPayload.class);
        return decryptedPayload;
    }

    public static DecryptedPayload decryptPayload(byte[] encryptedPayload, SessionCipher sessionCipher) throws LegacyMessageException, InvalidMessageException, JsonProcessingException {
        byte[] decryptedPayloadBytes = null;
        try {
            SignalMessage signalMessage = new SignalMessage(encryptedPayload);
            decryptedPayloadBytes = sessionCipher.decrypt(signalMessage);
        } catch (Exception e) {
            try {
                PreKeySignalMessage preKeySignalMessage = new PreKeySignalMessage(encryptedPayload);
                decryptedPayloadBytes = sessionCipher.decrypt(preKeySignalMessage);
            } catch (DuplicateMessageException duplicateMessageException) {
                duplicateMessageException.printStackTrace();
            } catch (InvalidKeyIdException invalidKeyIdException) {
                invalidKeyIdException.printStackTrace();
            } catch (InvalidKeyException invalidKeyException) {
                invalidKeyException.printStackTrace();
            } catch (UntrustedIdentityException untrustedIdentityException) {
                untrustedIdentityException.printStackTrace();
            } catch (InvalidVersionException invalidVersionException) {
                invalidVersionException.printStackTrace();
            }
        }

        String decryptedPayloadJson = new String(decryptedPayloadBytes, StandardCharsets.UTF_8);
        DecryptedPayload decryptedPayload = payloadObjectMapper.readValue(decryptedPayloadJson, DecryptedPayload.class);
        return decryptedPayload;
    }

    public static byte[] encryptPayload(DecryptedPayload decryptedPayload, SessionCipher sessionCipher) throws UntrustedIdentityException {
        String json = null;
        try {
            json = payloadObjectMapper.writeValueAsString(decryptedPayload);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        CiphertextMessage ciphertext = sessionCipher.encrypt(json.getBytes(StandardCharsets.UTF_8));
        return ciphertext.serialize();
    }

    public static InputStream decryptInputStream(InputStream inputStream, String encryptionKeyString, String iv) {
        if (cipher == null) {
            try {
                cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            }
        }

        byte[] encryptionKeyBytes = base64.decode(encryptionKeyString);
        SecretKey encryptionKey = new SecretKeySpec(encryptionKeyBytes, "AES");

        try {
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey, new IvParameterSpec(base64.decode(iv)));
        } catch (InvalidAlgorithmParameterException | java.security.InvalidKeyException e) {
            e.printStackTrace();
        }

        InputStream decryptedInputStream = new CipherInputStream(inputStream, cipher);

        return decryptedInputStream;
    }

    public static String identityKeyBytesToString(byte[] identityKeyBytes) {
        return base64.encodeAsString(identityKeyBytes);
    }

    public static String identityPublicKeyToString(IdentityKey identityKey) {
        return base64.encodeAsString(identityKey.serialize());
    }

    public static IdentityKey stringToIdentityPublicKey(String string) {
        IdentityKey identityKey = null;
        try {
            identityKey = new IdentityKey(base64.decode(string), 0);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return identityKey;
    }

    public static String getComparableFingerprint(Device device, LasendStore store) {
        FingerprintGenerator fingerprintGenerator = new NumericFingerprintGenerator(EncryptionConstants.FINGERPRINT_ITERATIONS);
        String myIdentity = store.getPersistent().getMeStore().getMe().getIdentity();
        Fingerprint fingerprint = fingerprintGenerator.createFor(EncryptionConstants.FINGERPRINT_VERSION, myIdentity.getBytes(),
                store.getPersistent().getMeStore().getMe().getSignalProtocolStore().getIdentityKeyPair().getPublicKey(), device.getIdentity().getBytes(), EncryptionUtil.stringToIdentityPublicKey(device.getIdentity()));
        DisplayableFingerprint displayableFingerprint = fingerprint.getDisplayableFingerprint();
        return displayableFingerprint.getDisplayText();
    }
}
