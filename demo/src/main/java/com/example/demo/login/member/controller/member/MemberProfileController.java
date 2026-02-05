package com.example.demo.login.member.controller.member;

import com.example.demo.common.exception.ApiResponse;
import com.example.demo.login.member.controller.auth.dto.MemberProfileResponse;
import com.example.demo.login.member.controller.auth.dto.MemberUpdateRequest;
import com.example.demo.login.member.domain.member.Member;
import com.example.demo.login.member.mapper.auth.AuthMapper;
import com.example.demo.login.member.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class MemberProfileController {

    private final AuthService authService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberProfileResponse>> getProfile(
            @com.example.demo.login.global.annotation.Member Long memberId
    ) {
        Member member = authService.getById(memberId);
        return ResponseEntity.ok(
                ApiResponse.success(AuthMapper.toMemberProfileResponse(member))
        );
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<String>> updateProfile(
            @com.example.demo.login.global.annotation.Member Long memberId,
            @RequestBody MemberUpdateRequest updateRequest
    ) {
        authService.updateProfile(memberId, updateRequest);
        return ResponseEntity.ok(ApiResponse.success("프로필이 수정되었습니다."));
    }

    @DeleteMapping("/member")
    public ResponseEntity<ApiResponse<String>> withdraw(
            @com.example.demo.login.global.annotation.Member Long memberId
    ) {
        authService.withdrawMember(memberId);
        return ResponseEntity.ok(ApiResponse.success("회원 탈퇴가 완료되었습니다."));
    }
}
