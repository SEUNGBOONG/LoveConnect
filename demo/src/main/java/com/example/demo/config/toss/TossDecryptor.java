package com.example.demo.config.toss;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class TossDecryptor {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;

    public static String decrypt(String encryptedBase64, String keyBase64, String aad) {
        try {
            // Secret 자체에 포함되었을지 모를 공백/줄바꿈 제거
            String cleanKey = keyBase64.trim();
            String cleanAad = aad.trim();

            byte[] decoded = Base64.getDecoder().decode(encryptedBase64);
            ByteBuffer buffer = ByteBuffer.wrap(decoded);

            // 1. IV 추출
            byte[] iv = new byte[IV_LENGTH_BYTE];
            buffer.get(iv);

            // 2. CipherText + Tag 분리
            byte[] cipherAndTag = new byte[buffer.remaining()];
            buffer.get(cipherAndTag);

            // 3. Cipher 초기화
            Cipher cipher = Cipher.getInstance(ALGORITHM);

            SecretKeySpec keySpec = new SecretKeySpec(
                    Base64.getDecoder().decode(cleanKey),
                    "AES"
            );

            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

            // 4. AAD 설정
            cipher.updateAAD(cleanAad.getBytes(StandardCharsets.UTF_8));

            // 5. 복호화 실행
            byte[] decrypted = cipher.doFinal(cipherAndTag);
            return new String(decrypted, StandardCharsets.UTF_8);

        } catch (Exception e) {
            // 에러 발생 시 로그 확인을 위해 원인 포함
            throw new IllegalStateException("토스 개인정보 복호화 실패: " + e.getMessage(), e);
        }
    }
}
