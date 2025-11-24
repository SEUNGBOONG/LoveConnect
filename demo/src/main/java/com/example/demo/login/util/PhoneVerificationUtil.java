package com.example.demo.login.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class PhoneVerificationUtil {

    private static final String PREFIX = "PHONE_AUTH_";
    private static final int CODE_LENGTH = 6;
    private static final Duration EXPIRATION = Duration.ofMinutes(3); // 인증번호 유효시간

    private final RedisUtil redisUtil;

    public String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10)); // 0 ~ 9
        }

        return code.toString();
    }

    public void saveVerificationCode(String phoneNumber, String code) {
        redisUtil.set(PREFIX + phoneNumber, code, EXPIRATION);
    }

    public boolean verifyCode(String phoneNumber, String inputCode) {
        String storedCode = redisUtil.get(PREFIX + phoneNumber);
        return storedCode != null && storedCode.equals(inputCode);
    }

    public void removeCode(String phoneNumber) {
        redisUtil.delete(PREFIX + phoneNumber);
    }

}
