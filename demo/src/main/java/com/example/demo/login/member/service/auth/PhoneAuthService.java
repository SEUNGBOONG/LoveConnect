package com.example.demo.login.member.service.auth;

import com.example.demo.login.util.AligoSmsUtil;
import com.example.demo.login.util.PhoneVerificationUtil;
import com.example.demo.login.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class PhoneAuthService {

    private final PhoneVerificationUtil phoneVerificationUtil;
    private final RedisUtil redisUtil;
    private final AligoSmsUtil aligoSmsUtil;

    private static final String VERIFIED_PREFIX = "PHONE_AUTH_SUCCESS_";

    public void sendVerificationCode(String phoneNumber) {
        String code = phoneVerificationUtil.generateCode();
        phoneVerificationUtil.saveVerificationCode(phoneNumber, code);
        System.out.println("📌 [개발용] 생성된 인증번호: " + code + " (전송 대상: " + phoneNumber + ")");
        String message = "[LoveConnect 인증번호] " + code + " (3분 내 입력)";
        aligoSmsUtil.sendSms(phoneNumber, message);
    }

    public boolean verifyCode(String phoneNumber, String inputCode) {
        boolean result = phoneVerificationUtil.verifyCode(phoneNumber, inputCode);
        if (result) {
            phoneVerificationUtil.removeCode(phoneNumber);
            redisUtil.set(VERIFIED_PREFIX + phoneNumber, "true", Duration.ofMinutes(10));
        }
        return result;
    }

    public boolean isVerified(String phoneNumber) {
        return "true".equals(redisUtil.get(VERIFIED_PREFIX + phoneNumber));
    }

    public void clearVerified(String phoneNumber) {
        redisUtil.delete(VERIFIED_PREFIX + phoneNumber);
    }
}
