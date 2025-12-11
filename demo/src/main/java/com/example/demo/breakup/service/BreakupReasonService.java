package com.example.demo.breakup.service;

import com.example.demo.breakup.domain.BreakupReason;
import com.example.demo.breakup.domain.repository.BreakupReasonRepository;
import com.example.demo.breakup.dto.BreakupReasonRequest;
import com.example.demo.login.global.exception.exceptions.CustomErrorCode;
import com.example.demo.login.global.exception.exceptions.CustomException;
import com.example.demo.login.member.domain.member.Member;
import com.example.demo.login.member.infrastructure.member.MemberJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BreakupReasonService {

    private final BreakupReasonRepository repository;
    private final MemberJpaRepository memberRepository;

    @Transactional
    public void saveReason(Long memberId, BreakupReasonRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));

        BreakupReason reason = BreakupReason.builder()
                .member(member)
                .reasons(request.reasons())
                .etcReason(request.etcReason())
                .build();

        repository.save(reason);
    }
}
