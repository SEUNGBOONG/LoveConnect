package com.example.demo.login.member.controller.member;

import com.example.demo.common.exception.ApiResponse;
import com.example.demo.login.global.annotation.Member;
import com.example.demo.login.member.controller.member.dto.TiktokIdRequest;
import com.example.demo.login.member.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final AuthService authService;

    @PutMapping("/tiktok")
    public ResponseEntity<ApiResponse<String>> registerTiktokId(
            @Member Long memberId,
            @RequestBody TiktokIdRequest request
    ) {
        authService.registerTiktokId(memberId, request.getTiktokId());
        return ResponseEntity.ok(ApiResponse.success("틱톡 아이디 등록 완료"));
    }
}
