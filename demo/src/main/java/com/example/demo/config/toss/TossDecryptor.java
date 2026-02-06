package com.example.demo.config.toss; // 패키지 경로는 형님 환경에 맞게!

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class TossDecryptor {

    // 반드시 'static'이 붙어 있어야 합니다! 그래야 TossAuthService에서 바로 씁니다.
    public static String decrypt(String encryptedText, String base64EncodedAesKey, String aad) throws Exception {

        final int IV_LENGTH = 12;
        final int TAG_BIT_LENGTH = 128;

        byte[] decodedData = Base64.getDecoder().decode(encryptedText.trim());
        byte[] keyByteArray = Base64.getDecoder().decode(base64EncodedAesKey.trim());

        byte[] iv = new byte[IV_LENGTH];
        System.arraycopy(decodedData, 0, iv, 0, IV_LENGTH);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(keyByteArray, "AES");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_BIT_LENGTH, iv);

        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

        if (aad != null && !aad.isEmpty()) {
            cipher.updateAAD(aad.getBytes(StandardCharsets.UTF_8));
        }

        byte[] decrypted = cipher.doFinal(decodedData, IV_LENGTH, decodedData.length - IV_LENGTH);

        return new String(decrypted, StandardCharsets.UTF_8);
    }
}
