package com.example.demo.match.domain;

import com.example.demo.login.member.domain.member.Member;
import com.example.demo.match.domain.MatchRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MatchRequestRepository extends JpaRepository<MatchRequest, Long> {
    Optional<MatchRequest> findByRequester(Member requester);
    Optional<MatchRequest> findByTargetPhoneNumberAndTargetInstagramIdAndMatchedFalse(String phone, String insta);
    boolean existsByRequester(Member requester);
}
