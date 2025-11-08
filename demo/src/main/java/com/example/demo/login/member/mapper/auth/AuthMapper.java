package com.example.demo.login.member.mapper.auth;

import com.example.demo.common.util.AESUtil;
import com.example.demo.login.member.controller.auth.dto.LoginResponse;
import com.example.demo.login.member.controller.auth.dto.NormalSignUpRequest;
import com.example.demo.login.member.controller.auth.dto.SignUpResponse;
import com.example.demo.login.member.domain.member.Member;

public class AuthMapper {

    public static Member toNormalMember(NormalSignUpRequest request, String encodedPassword) {
        return Member.builder()
                .memberEmail(request.email().trim().toLowerCase())
                .memberName(request.name().trim())
                .memberPassword(encodedPassword)
                .memberNickName(request.nickname().trim())
                .phoneNumber(AESUtil.encrypt(request.phoneNumber().trim()))
                .instagramId(AESUtil.encrypt(request.instagramId().trim().toLowerCase()))
                .mbti(request.mbti().trim().toUpperCase())
                .birthDate(request.birthDate())
                .build();
    }

    public static SignUpResponse toSignUpResponse(Member member) {
        return new SignUpResponse(
                member.getId(),
                member.getMemberName(),
                member.getMemberEmail(),
                member.getMemberNickName()
        );
    }

    public static LoginResponse toLoginResponse(Member member) {
        return new LoginResponse(
                member.getId(),
                member.getMemberName(),
                member.getMemberNickName()
        );
    }
}
