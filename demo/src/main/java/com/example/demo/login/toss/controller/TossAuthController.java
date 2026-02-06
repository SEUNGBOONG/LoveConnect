package com.example.demo.login.toss.controller;

import com.example.demo.login.global.annotation.Member;
import com.example.demo.login.toss.application.TossAuthService;
import com.example.demo.login.toss.dto.request.TossAdditionalInfoRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/toss")
@RequiredArgsConstructor
public class TossAuthController {

    private final TossAuthService tossAuthService;

    @PostMapping("/login")
    // [수정] 서비스에서 던지는 예외를 상위(스프링 핸들러)로 넘깁니다.
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody Map<String, String> body
    ) throws Exception {

        String authorizationCode = body.get("authorizationCode");
        String referrer = body.get("referrer");

        return ResponseEntity.ok(
                tossAuthService.executeTossLogin(
                        authorizationCode,
                        referrer
                )
        );
    }

    @PatchMapping("/additional-info")
    public ResponseEntity<Void> additional(
            @Member Long memberId,
            @RequestBody TossAdditionalInfoRequest request
    ) {
        tossAuthService.updateMemberProfile(memberId, request);
        return ResponseEntity.ok().build();
    }
}
