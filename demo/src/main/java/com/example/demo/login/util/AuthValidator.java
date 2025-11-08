package com.example.demo.login.util;

import com.example.demo.login.member.domain.member.Member;
import com.example.demo.login.member.exception.exceptions.auth.DuplicateEmailException;
import com.example.demo.login.member.exception.exceptions.auth.DuplicateNickNameException;
import com.example.demo.login.member.exception.exceptions.auth.NotFoundMemberByEmailException;
import com.example.demo.login.member.exception.exceptions.auth.NotSamePasswordException;
import com.example.demo.login.member.infrastructure.member.MemberJpaRepository;
import org.springframework.stereotype.Component;

@Component
public class AuthValidator {

    private final MemberJpaRepository memberJpaRepository;

    public AuthValidator(final MemberJpaRepository memberJpaRepository) {
        this.memberJpaRepository = memberJpaRepository;
    }

    public static void validatePasswordEncoderException(final boolean passwordEncoder) {
        extracted(passwordEncoder);
    }

    public Member findMemberByEmail(String email) {
        return memberJpaRepository.findMemberByMemberEmail(email)
                .orElseThrow(NotFoundMemberByEmailException::new);
    }

    public void checkDuplicateMemberNickName(String nickName) {
        if (memberJpaRepository.existsByMemberNickName(nickName)) {
            throw new DuplicateNickNameException();
        }
    }

    public void checkDuplicateMemberEmail(String email) {
        if (memberJpaRepository.existsByMemberEmail(email)) {
            throw new DuplicateEmailException();
        }
    }

    private static void extracted(final boolean passwordEncoder) {
        if (!passwordEncoder) {
            throw new NotSamePasswordException();
        }
    }
}
