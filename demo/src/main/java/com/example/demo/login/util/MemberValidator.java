package com.example.demo.login.util;

import com.example.demo.login.member.domain.member.Member;
import com.example.demo.login.member.exception.exceptions.auth.NotFoundMemberByEmailException;
import com.example.demo.login.member.infrastructure.member.MemberJpaRepository;
import org.springframework.stereotype.Component;

@Component
public class MemberValidator {

    private final MemberJpaRepository memberJpaRepository;

    public MemberValidator(final MemberJpaRepository memberJpaRepository) {
        this.memberJpaRepository = memberJpaRepository;
    }

    public Member getMember(final Long memberId) {
        return memberJpaRepository.findById(memberId)
                .orElseThrow(NotFoundMemberByEmailException::new);
    }
}
