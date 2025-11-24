package com.example.demo.login.member.controller.auth;

import com.example.demo.common.exception.ApiResponse;
import com.example.demo.login.member.controller.auth.dto.PhoneRequest;
import com.example.demo.login.member.controller.auth.dto.PhoneVerificationRequest;
import com.example.demo.login.member.service.auth.PhoneAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/phone")
public class PhoneAuthController {

    private final PhoneAuthService phoneAuthService;

    /**
     * 1️⃣ 인증번호 발송 API
     */

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<String>> sendCode(@RequestBody PhoneRequest request) {
        log.info("📨 [문자 발송 요청] 대상 번호: {}", request.phoneNumber());
        phoneAuthService.sendVerificationCode(request.phoneNumber());
        return ResponseEntity.ok(ApiResponse.success("인증번호가 전송되었습니다."));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<String>> verifyCode(@RequestBody PhoneVerificationRequest request) {
        log.info("✅ [문자 인증 요청] 대상 번호: {}, 입력 코드: {}", request.phoneNumber(), request.verificationCode());

        boolean result = phoneAuthService.verifyCode(request.phoneNumber(), request.verificationCode());

        if (result) {
            log.info("🔒 [문자 인증 성공] {}", request.phoneNumber());
            return ResponseEntity.ok(ApiResponse.success("전화번호 인증 완료"));
        } else {
            log.warn("❌ [문자 인증 실패] 번호: {}, 입력된 코드: {}", request.phoneNumber(), request.verificationCode());
            return ResponseEntity.status(400)
                    .body(ApiResponse.fail("PHONE_VERIFICATION_FAIL", "인증번호가 일치하지 않습니다."));
        }
    }
}
