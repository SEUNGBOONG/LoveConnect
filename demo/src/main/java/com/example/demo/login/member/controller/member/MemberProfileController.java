package com.example.demo.login.member.controller.member;

import com.example.demo.common.exception.ApiResponse;
import com.example.demo.login.member.controller.auth.dto.MemberProfileResponse;
import com.example.demo.login.member.controller.auth.dto.MemberUpdateRequest;
import com.example.demo.login.member.domain.member.Member;
import com.example.demo.login.member.mapper.auth.AuthMapper;
import com.example.demo.login.member.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class MemberProfileController {

    private final AuthService authService;

    private Long getCurrentMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) return null;

        try {
            return Long.parseLong(authentication.getPrincipal().toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ✅ 내 프로필 조회
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberProfileResponse>> getProfile() {
        Long memberId = getCurrentMemberId();
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
            @RequestBody MemberUpdateRequest updateRequest
    ) {
        Long memberId = getCurrentMemberId();
        if (memberId == null) {
            return ResponseEntity
                    .status(401)
                    .body(ApiResponse.fail("UNAUTHORIZED", "로그인이 필요합니다."));
        }

        authService.updateProfile(memberId, updateRequest);
        return ResponseEntity.ok(ApiResponse.success("프로필이 수정되었습니다."));
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<String>> withdraw() {
        Long memberId = getCurrentMemberId();
        if (memberId == null) {
            return ResponseEntity.status(401).body(ApiResponse.fail("UNAUTHORIZED", "로그인이 필요합니다."));
        }

        authService.withdrawMember(memberId);
        return ResponseEntity.ok(ApiResponse.success("회원 탈퇴가 완료되었습니다."));
    }
}
