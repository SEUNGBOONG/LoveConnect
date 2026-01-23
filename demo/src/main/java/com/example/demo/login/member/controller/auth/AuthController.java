package com.example.demo.login.member.controller.auth;

import com.example.demo.common.exception.ApiResponse;
import com.example.demo.login.global.annotation.LoginMember;
import com.example.demo.login.member.controller.auth.dto.LoginRequest;
import com.example.demo.login.member.controller.auth.dto.LoginResponse;
import com.example.demo.login.member.controller.auth.dto.PasswordResetRequest;
import com.example.demo.login.member.domain.member.Member;
import com.example.demo.login.member.mapper.auth.AuthMapper;
import com.example.demo.login.member.service.auth.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @RequestBody LoginRequest loginRequest,
            HttpServletResponse response
    ) {
        Member member = authService.loginAndReturnMember(loginRequest);
        String token = authService.generateToken(member.getId());

        Cookie jwtCookie = new Cookie("token", token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(60 * 60);
        jwtCookie.setSecure(true);
        jwtCookie.setDomain(".lovereconnect.co.kr");
        response.addCookie(jwtCookie);

        return ResponseEntity.ok(
                ApiResponse.success(AuthMapper.toLoginResponse(member))
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletResponse response) {
        Cookie deleteCookie = new Cookie("token", null);
        deleteCookie.setDomain(".lovereconnect.co.kr");
        deleteCookie.setPath("/");
        deleteCookie.setMaxAge(0);
        deleteCookie.setSecure(true);
        response.addCookie(deleteCookie);

        return ResponseEntity.ok(ApiResponse.success("로그아웃 되었습니다."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @RequestBody PasswordResetRequest request
    ) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 🔥 로그인 상태 체크 전용
    @GetMapping("/auth/me")
    public ResponseEntity<ApiResponse<?>> me(@LoginMember Long memberId) {
        Member member = authService.getById(memberId);
        return ResponseEntity.ok(
                ApiResponse.success(
                        AuthMapper.toMemberProfileResponse(member)
                )
        );
    }
}
