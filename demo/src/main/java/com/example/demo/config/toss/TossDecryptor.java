package com.example.demo.config.toss;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class TossDecryptor {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;   // 16 bytes
    private static final int IV_LENGTH_BYTE = 12;    // Toss spec

    public static String decrypt(String encryptedBase64, String keyBase64, String aad) {
        try {
            byte[] decoded = Base64.getDecoder().decode(encryptedBase64);
            ByteBuffer buffer = ByteBuffer.wrap(decoded);

            // 1️⃣ IV 추출
            byte[] iv = new byte[IV_LENGTH_BYTE];
            buffer.get(iv);

            // 2️⃣ CipherText + Tag 분리
            byte[] cipherAndTag = new byte[buffer.remaining()];
            buffer.get(cipherAndTag);

            // 3️⃣ Cipher 초기화
            Cipher cipher = Cipher.getInstance(ALGORITHM);

            SecretKeySpec keySpec = new SecretKeySpec(
                    Base64.getDecoder().decode(keyBase64),
                    "AES"
            );

            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

            // 4️⃣ AAD 반드시 동일해야 함
            cipher.updateAAD(aad.getBytes(StandardCharsets.UTF_8));

            // 5️⃣ 복호화
            byte[] decrypted = cipher.doFinal(cipherAndTag);
            return new String(decrypted, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new IllegalStateException("토스 개인정보 복호화 실패", e);
        }
    }
}
