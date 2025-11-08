package com.example.demo.login.email.util;

import com.example.demo.login.email.domain.EmailCode;
import com.example.demo.login.email.infrastruture.EmailForm;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class EmailSenderUtil {

    private final JavaMailSender emailSender;
    private final EmailCode emailCode;
    private final EmailForm form;
    private final StringRedisTemplate redisTemplate;

    private static final long AUTH_CODE_TTL_MINUTES = 5;
    private static final long VERIFIED_FLAG_TTL_MINUTES = 10;

    public String sendEmail(String toEmail) throws MessagingException, UnsupportedEncodingException {
        String authNum = emailCode.createCode();
        redisTemplate.opsForValue().set(toEmail, authNum, Duration.ofMinutes(AUTH_CODE_TTL_MINUTES));
        MimeMessage emailForm = form.createEmailForm(toEmail, authNum);
        emailSender.send(emailForm);
        return authNum;
    }

    public boolean verifyAuthCode(String email, String inputCode) {
        String storedCode = redisTemplate.opsForValue().get(email);
        if (inputCode.equals(storedCode)) {
            redisTemplate.delete(email); // 인증번호 삭제
            redisTemplate.opsForValue().set(getVerifiedKey(email), "true", Duration.ofMinutes(VERIFIED_FLAG_TTL_MINUTES));
            return true;
        }
        return false;
    }

    public boolean isEmailVerified(String email) {
        return "true".equals(redisTemplate.opsForValue().get(getVerifiedKey(email)));
    }

    public void clearVerifiedFlag(String email) {
        redisTemplate.delete(getVerifiedKey(email));
    }

    private String getVerifiedKey(String email) {
        return "auth:verified:" + email;
    }
}
