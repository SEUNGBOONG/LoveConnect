package com.example.demo.login.member.mapper.auth;

import com.example.demo.common.util.AESUtil;
import com.example.demo.login.member.controller.auth.dto.LoginResponse;
import com.example.demo.login.member.controller.auth.dto.MemberProfileResponse;
import com.example.demo.login.member.controller.auth.dto.NormalSignUpRequest;
import com.example.demo.login.member.controller.auth.dto.SignUpResponse;
import com.example.demo.login.member.domain.member.Member;

public class AuthMapper {

    public static MemberProfileResponse toMemberProfileResponse(Member member) {
        return new MemberProfileResponse(
                member.getMemberEmail(),
                member.getMemberName(),
                member.getMemberNickName(),
                AESUtil.decrypt(member.getPhoneNumber()),
                AESUtil.decrypt(member.getInstagramId()),
                AESUtil.decrypt(member.getTiktokId()),
                member.getMbti(),
                member.getGender(),
                member.getBirthDate(),
                member.isEmailAgree()
        );
    }


    public static Member toNormalMember(NormalSignUpRequest request, String encodedPassword) {
        String birthDate = request.birthYear().substring(2)
                + String.format("%02d", Integer.parseInt(request.birthMonth()))
                + String.format("%02d", Integer.parseInt(request.birthDay()));

        return Member.builder()
                .memberEmail(request.email())
                .memberName(request.name())
                .memberPassword(encodedPassword)
                .memberNickName(request.nickname())
                .phoneNumber(AESUtil.encrypt(request.phoneNumber()))
                .instagramId(AESUtil.encrypt(request.instagramId()))
                .tiktokId(request.tiktokId())
                .mbti(request.mbti())
                .birthDate(birthDate)
                .gender(request.gender())
                .emailAgree(request.emailAgree())
                .privacyAgree(request.privacyAgree())
                .useAgree(request.useAgree())
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
                member.getMemberNickName(),
                AESUtil.decrypt(member.getTiktokId()) // null-safe
        );
    }
}
