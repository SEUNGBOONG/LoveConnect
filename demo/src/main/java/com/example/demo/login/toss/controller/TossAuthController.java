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
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(
                    tossAuthService.executeTossLogin(body.get("authorizationCode"))
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "message", "서버 오류"
            ));
        }
    }

    @PatchMapping("/additional-info")
    public ResponseEntity<Void> additional(
            @Member Long memberId,
            @RequestBody TossAdditionalInfoRequest request) {

        tossAuthService.updateMemberProfile(memberId, request);
        return ResponseEntity.ok().build();
    }
}
