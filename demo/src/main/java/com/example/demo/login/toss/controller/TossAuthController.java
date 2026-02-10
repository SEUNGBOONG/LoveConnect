package com.example.demo.login.toss.controller;

import com.example.demo.login.global.annotation.Member;
import com.example.demo.login.toss.application.TossAuthService;
import com.example.demo.login.toss.dto.request.TossAdditionalInfoRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody Map<String, String> body,
            HttpServletResponse response,
            HttpServletRequest request
    ) throws Exception {

        String authorizationCode = body.get("authorizationCode");
        String referrer = body.get("referrer");

        Map<String, Object> result = tossAuthService.executeTossLogin(authorizationCode, referrer);
        String token = (String) result.get("token");

        // ✅ 쿠키 세팅
        Cookie jwtCookie = new Cookie("token", token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(60 * 60); // 1시간

        boolean isLocal =
                request.getServerName().equals("localhost") || request.getServerName().equals("127.0.0.1");

        if (!isLocal) {
            jwtCookie.setSecure(true);
            jwtCookie.setDomain(".lovereconnect.co.kr");
        }

        response.addCookie(jwtCookie);

        // ✅ 이걸로 token, memberId 등 다 내려보냄
        return ResponseEntity.ok(result);
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
