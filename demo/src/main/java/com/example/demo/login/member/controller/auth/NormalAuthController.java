package com.example.demo.login.member.controller.auth;

import com.example.demo.common.exception.ApiResponse;
import com.example.demo.login.member.controller.auth.dto.NormalSignUpRequest;
import com.example.demo.login.member.controller.auth.dto.SignUpResponse;
import com.example.demo.login.member.mapper.auth.AuthMapper;
import com.example.demo.login.member.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
@Slf4j
public class NormalAuthController {

    private final AuthService authService;

    @PostMapping("/normalMembers")
    public ResponseEntity<ApiResponse<SignUpResponse>> normalSignUp(@RequestBody NormalSignUpRequest request) {
        SignUpResponse response = AuthMapper.toSignUpResponse(authService.normalSignUp(request));
        return ResponseEntity.status(201).body(ApiResponse.success(response)); // 201 Created
    }
}
