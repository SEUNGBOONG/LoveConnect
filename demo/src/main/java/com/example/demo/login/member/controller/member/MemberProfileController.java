package com.example.demo.login.member.controller.member;

import com.example.demo.common.exception.ApiResponse;
import com.example.demo.login.member.controller.auth.dto.MemberProfileResponse;
import com.example.demo.login.member.controller.auth.dto.MemberUpdateRequest;
import com.example.demo.login.member.domain.member.Member;
import com.example.demo.login.member.mapper.auth.AuthMapper;
import com.example.demo.login.member.service.auth.AuthService;
import com.example.demo.login.member.infrastructure.auth.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class MemberProfileController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    private Long extractMemberIdFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        for (Cookie cookie : request.getCookies()) {
            if ("token".equals(cookie.getName())) {
                return jwtTokenProvider.getMemberIdFromToken(cookie.getValue());
            }
        }
        return null;
    }

    // ✅ 내 프로필 조회
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberProfileResponse>> getProfile(HttpServletRequest request) {
        Long memberId = extractMemberIdFromCookie(request);
        if (memberId == null) {
            return ResponseEntity
                    .status(401)
                    .body(ApiResponse.fail("UNAUTHORIZED", "로그인이 필요합니다."));
        }

        Member member = authService.getById(memberId);
        return ResponseEntity.ok(ApiResponse.success(AuthMapper.toMemberProfileResponse(member)));
    }

    // ✅ 내 프로필 수정
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<String>> updateProfile(
            HttpServletRequest request,
            @RequestBody MemberUpdateRequest updateRequest
    ) {
        Long memberId = extractMemberIdFromCookie(request);
        if (memberId == null) {
            return ResponseEntity
                    .status(401)
                    .body(ApiResponse.fail("UNAUTHORIZED", "로그인이 필요합니다."));
        }

        authService.updateProfile(memberId, updateRequest);
        return ResponseEntity.ok(ApiResponse.success("프로필이 수정되었습니다."));
    }
}
