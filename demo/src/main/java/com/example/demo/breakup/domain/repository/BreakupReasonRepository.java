package com.example.demo.breakup.domain.repository;

import com.example.demo.breakup.domain.BreakupReason;
import com.example.demo.login.member.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BreakupReasonRepository extends JpaRepository<BreakupReason, Long> {
    Optional<BreakupReason> findByMember(Member member);
}
