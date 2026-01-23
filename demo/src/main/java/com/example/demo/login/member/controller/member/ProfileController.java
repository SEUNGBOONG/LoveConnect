package com.example.demo.login.member.controller.member;

import com.example.demo.common.exception.ApiResponse;
import com.example.demo.common.util.AESUtil;
import com.example.demo.login.global.annotation.Member;
import com.example.demo.login.global.exception.exceptions.CustomErrorCode;
import com.example.demo.login.global.exception.exceptions.CustomException;
import com.example.demo.login.member.controller.member.dto.TiktokIdRequest;
import com.example.demo.login.member.infrastructure.member.MemberJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final MemberJpaRepository memberRepository;

    @PostMapping("/tiktok")
    public ResponseEntity<ApiResponse<String>> registerTiktokId(
            @Member Long memberId,
            @RequestBody TiktokIdRequest request
    ) {
        com.example.demo.login.member.domain.member.Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MATCH_MEMBER_NOT_FOUND));

        if (member.getTiktokId() != null) {
            throw new CustomException(CustomErrorCode.ALREADY_REGISTERED);
        }

        member.updateTiktokId(AESUtil.encrypt(request.getTiktokId().trim().toLowerCase()));
        return ResponseEntity.ok(ApiResponse.success("틱톡 아이디 등록 완료"));
    }
}
