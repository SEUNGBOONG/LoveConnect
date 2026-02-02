package com.example.demo.login.member.controller.auth;

import com.example.demo.common.exception.ApiResponse;
import com.example.demo.login.member.controller.auth.dto.LoginRequest;
import com.example.demo.login.member.controller.auth.dto.LoginResponse;
import com.example.demo.login.member.controller.auth.dto.PasswordResetRequest;
import com.example.demo.login.member.domain.member.Member;
import com.example.demo.login.member.mapper.auth.AuthMapper;
import com.example.demo.login.member.service.auth.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @RequestBody LoginRequest loginRequest,
            HttpServletResponse response,
            HttpServletRequest request
    ) {
        Member member = authService.loginAndReturnMember(loginRequest);
        String token = authService.generateToken(member.getId());

        Cookie jwtCookie = new Cookie("token", token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(60 * 60);

        boolean isLocal =
                request.getServerName().equals("localhost")
                        || request.getServerName().equals("127.0.0.1");

        if (!isLocal) {
            jwtCookie.setSecure(true);
            jwtCookie.setDomain(".lovereconnect.co.kr");
        }

        response.addCookie(jwtCookie);

        LoginResponse loginResponse = AuthMapper.toLoginResponse(member);
        return ResponseEntity.ok(ApiResponse.success(loginResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            HttpServletResponse response,
            HttpServletRequest request
    ) {
        Cookie deleteCookie = new Cookie("token", null);
        deleteCookie.setPath("/");
        deleteCookie.setMaxAge(0);

        boolean isLocal =
                request.getServerName().equals("localhost")
                        || request.getServerName().equals("127.0.0.1");

        if (!isLocal) {
            deleteCookie.setSecure(true);
            deleteCookie.setDomain(".lovereconnect.co.kr");
        }

        response.addCookie(deleteCookie);

        return ResponseEntity.ok(ApiResponse.success("로그아웃 되었습니다."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@RequestBody PasswordResetRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/auth/me")
    public ResponseEntity<ApiResponse<?>> me(@com.example.demo.login.global.annotation.Member Long memberId) {
        Member member = authService.getById(memberId);
        return ResponseEntity.ok(
                ApiResponse.success(
                        AuthMapper.toMemberProfileResponse(member)
                )
        );
    }

}
