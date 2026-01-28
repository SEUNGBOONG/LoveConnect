package com.example.demo.config.toss;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.util.Base64;

public class TossDecryptor {
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;

    public static String decrypt(String encryptedBase64, String key, String aad) throws Exception {
        byte[] decode = Base64.getDecoder().decode(encryptedBase64);
        ByteBuffer byteBuffer = ByteBuffer.wrap(decode);

        byte[] iv = new byte[IV_LENGTH_BYTE];
        byteBuffer.get(iv);

        byte[] cipherText = new byte[byteBuffer.remaining()];
        byteBuffer.get(cipherText);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(Base64.getDecoder().decode(key), "AES");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);

        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
        cipher.updateAAD(aad.getBytes());

        return new String(cipher.doFinal(cipherText), "UTF-8");
    }
}
