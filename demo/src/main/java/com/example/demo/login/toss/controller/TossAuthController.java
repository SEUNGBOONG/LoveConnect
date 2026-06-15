package com.example.demo.login.toss.controller;

import com.example.demo.config.jwt.AuthCookieSupport;
import com.example.demo.login.global.annotation.Member;
import com.example.demo.login.member.infrastructure.auth.JwtTokenProvider;
import com.example.demo.login.toss.application.TossAuthService;
import com.example.demo.login.toss.dto.request.TossAdditionalInfoRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
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
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthCookieSupport authCookieSupport;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody Map<String, String> body,
            HttpServletResponse response,
            HttpServletRequest request
    ) throws Exception {

        String authorizationCode = body.get("authorizationCode");
        String referrer = body.get("referrer");

        if (!StringUtils.hasText(authorizationCode)) {
            throw new IllegalArgumentException("authorizationCode is required");
        }

        Map<String, Object> result = tossAuthService.executeTossLogin(authorizationCode, referrer);
        String token = (String) result.get("token");

        authCookieSupport.writeTokenCookie(
                request,
                response,
                token,
                (int) jwtTokenProvider.getExpirationPeriodSeconds()
        );

        return ResponseEntity.ok(result);
    }

    /**
     * ✅ 토스 로그아웃
     * - JWT 쿠키(token) 삭제만 수행 (서버 세션 없음)
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        authCookieSupport.clearTokenCookie(request, response);
        return ResponseEntity.ok().build();
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
