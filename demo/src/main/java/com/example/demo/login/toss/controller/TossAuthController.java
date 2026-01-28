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
            // 서비스에서 Map 형태로 결과를 받아옴
            Map<String, Object> result = tossAuthService.executeTossLogin(authCode);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
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

