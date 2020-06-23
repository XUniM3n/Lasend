package org.lasend.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
public class SharedFile implements GetIdAble {
    private static final Base64 base64 = new Base64();
    private static final SecureRandom secureRandom = new SecureRandom();
    private static KeyGenerator keyGenerator;
    private static Cipher cipher;
    @Getter
    private String path;
    @Getter
    private String name;
    @Getter
    private long size;
    @Getter
    private String hash;
    @Getter
    private String encryptionKey;
    @Getter
    private String iv;
    @Getter
    private String id;

    public SharedFile(File file) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException();
        }
        this.path = file.getAbsolutePath();
        this.name = file.getName();
        this.size = file.length();
        this.id = UUID.randomUUID().toString();

        try (InputStream is = new FileInputStream(file)) {
            hash = DigestUtils.sha512Hex(is);
        }

        if (keyGenerator == null) {
            try {
                keyGenerator = KeyGenerator.getInstance("AES");
                keyGenerator.init(256, secureRandom);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        SecretKey secretKey = keyGenerator.generateKey();
        encryptionKey = base64.encodeToString(secretKey.getEncoded());

        byte[] ivBytes = new byte[16];
        secureRandom.nextBytes(ivBytes);

        iv = base64.encodeAsString(ivBytes);
    }

    private SecretKey getSecretKey() {
        byte[] encryptionKeyBytes = base64.decode(encryptionKey);
        return new SecretKeySpec(encryptionKeyBytes, "AES");
    }

    public InputStream getEncryptedInputStream() throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            throw new FileNotFoundException();
        }

        if (cipher == null) {
            try {
                cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            }
        }

        try {
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), new IvParameterSpec(base64.decode(iv)));
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

        CipherInputStream encryptedInputStream;
        FileInputStream fileInputStream = new FileInputStream(file);
        encryptedInputStream = new CipherInputStream(fileInputStream, cipher);

        return encryptedInputStream;
    }
}
