package com.example.demo.config.toss;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class TossDecryptor {

    // static 키워드 추가로 TossAuthService에서 바로 호출 가능하게 수정
    public static String decrypt(String encryptedText, String base64EncodedAesKey, String aad) throws Exception {
        if (encryptedText == null || base64EncodedAesKey == null) {
            throw new IllegalArgumentException("암호문 또는 키값이 비어있습니다.");
        }

        final int IV_LENGTH = 12; // 토스 명세: IV 12바이트
        final int TAG_BIT_LENGTH = 128; // GCM 권장 태그 길이

        // 1. Base64 디코딩 (공백 제거)
        byte[] decodedData = Base64.getDecoder().decode(encryptedText.trim());
        byte[] keyByteArray = Base64.getDecoder().decode(base64EncodedAesKey.trim());

        // 2. IV 추출 (데이터의 앞 12바이트)
        byte[] iv = new byte[IV_LENGTH];
        System.arraycopy(decodedData, 0, iv, 0, IV_LENGTH);

        // 3. Cipher 설정 (AES/GCM/NoPadding)
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(keyByteArray, "AES");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_BIT_LENGTH, iv);

        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

        // 4. AAD(Additional Authenticated Data) 설정
        if (aad != null && !aad.isBlank()) {
            cipher.updateAAD(aad.getBytes(StandardCharsets.UTF_8));
        }

        // 5. 복호화 실행 (IV 이후의 데이터부터)
        byte[] decrypted = cipher.doFinal(decodedData, IV_LENGTH, decodedData.length - IV_LENGTH);

        return new String(decrypted, StandardCharsets.UTF_8);
    }
}
