package com.example.demo.match.domain;

import com.example.demo.login.member.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MatchRequestRepository extends JpaRepository<MatchRequest, Long> {

    Optional<MatchRequest> findByRequester(Member requester);

    Optional<MatchRequest> findByTargetPhoneNumberAndTargetInstagramIdAndMatchedFalseAndStatus(
            String phone,
            String insta,
            MatchStatus status
    );

    boolean existsByRequester(Member requester);
}
