package com.example.demo.login.util;

import com.example.demo.login.member.domain.member.Member;
import com.example.demo.login.member.infrastructure.member.MemberJpaRepository;
import com.example.demo.login.global.exception.exceptions.CustomErrorCode;
import com.example.demo.login.global.exception.exceptions.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthValidator {

    private final MemberJpaRepository memberJpaRepository;

    /** 📌 이메일로 멤버 찾기 */
    public Member findMemberByEmail(String email) {
        return memberJpaRepository.findMemberByMemberEmail(email)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MATCH_MEMBER_NOT_FOUND));
    }

    /** 📌 닉네임 중복 체크 (⭐핵심⭐) */
    public void checkDuplicateMemberNickName(String nickname) {
        if (memberJpaRepository.existsByMemberNickName(nickname)) {
            throw new CustomException(CustomErrorCode.DUPLICATE_NICKNAME);
        }
    }

    /** 📌 이메일 중복 체크 */
    public void checkDuplicateMemberEmail(String email) {
        if (memberJpaRepository.existsByMemberEmail(email)) {
            throw new CustomException(CustomErrorCode.DUPLICATE_EMAIL);
        }
    }

    /** 📌 비밀번호 match */
    public static void validatePasswordMatch(boolean isMatch) {
        if (!isMatch) {
            throw new CustomException(CustomErrorCode.NOT_SAME_PASSWORD);
        }
    }
}
