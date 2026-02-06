package com.example.demo.config.toss;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class TossDecryptor {

    public static String decrypt(String encryptedText, String base64EncodedAesKey, String aad) throws Exception {
        // 1. 상수 정의 (토스 명세: IV 12바이트, Tag 128비트)
        final int IV_LENGTH = 12;
        final int TAG_BIT_LENGTH = 128;

        // 2. Base64 디코딩 (입력값 정제 포함)
        byte[] decodedData = Base64.getDecoder().decode(encryptedText.trim());
        byte[] keyByteArray = Base64.getDecoder().decode(base64EncodedAesKey.trim());

        // 3. IV 추출 (데이터의 앞 12바이트)
        byte[] iv = new byte[IV_LENGTH];
        System.arraycopy(decodedData, 0, iv, 0, IV_LENGTH);

        // 4. Cipher 초기화
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(keyByteArray, "AES");

        // [핵심] 여기서 128(bit)이 정확히 들어가야 Tag mismatch가 안 납니다.
        GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_BIT_LENGTH, iv);

        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

        // 5. AAD 설정 (UTF-8 명시)
        if (aad != null) {
            cipher.updateAAD(aad.getBytes(StandardCharsets.UTF_8));
        }

        // 6. 복호화 실행 (IV 이후의 데이터 전체를 넘김)
        // 토스 예제 방식: cipher.doFinal(원본, 시작지점, 길이)
        byte[] decrypted = cipher.doFinal(decodedData, IV_LENGTH, decodedData.length - IV_LENGTH);

        return new String(decrypted, StandardCharsets.UTF_8);
    }
}
