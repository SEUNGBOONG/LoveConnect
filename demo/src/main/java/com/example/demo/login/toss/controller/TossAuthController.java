package com.example.demo.login.toss.controller;

import com.example.demo.login.global.annotation.Member;
import com.example.demo.login.toss.application.TossAuthService;
import com.example.demo.login.toss.dto.request.TossAdditionalInfoRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/toss")
@RequiredArgsConstructor
public class TossAuthController {

    private final TossAuthService tossAuthService;
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        try {
            String authCode = request.get("code");
            if (authCode == null || authCode.isEmpty()) {
                throw new IllegalArgumentException("토스 인증 코드가 없습니다.");
            }

            Map<String, Object> result = tossAuthService.executeTossLogin(authCode);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            // 이 로그가 도커 로그(docker logs -f loveconnect-app)에 찍힙니다.
            System.err.println("=== 토스 로그인 에러 발생 ===");
            e.printStackTrace();

            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", (e.getMessage() != null) ? e.getMessage() : "서버 내부 에러(NPE 가능성)"
            ));
        }
    }

    // TossAuthController 내부에 추가
    @PatchMapping("/additional-info")
    public ResponseEntity<Map<String, Object>> updateAdditionalInfo(
            @Member Long memberId,  // <-- Resolver가 토큰에서 ID를 꺼내 넣어줍니다.
            @RequestBody TossAdditionalInfoRequest request) {

        tossAuthService.updateMemberProfile(memberId, request);
        return ResponseEntity.ok(Map.of("success", true, "message", "프로필 업데이트 완료"));
    }
}

