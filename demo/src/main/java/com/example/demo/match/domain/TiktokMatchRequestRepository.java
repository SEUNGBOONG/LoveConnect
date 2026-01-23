package com.example.demo.match.domain;

import com.example.demo.login.member.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TiktokMatchRequestRepository extends JpaRepository<TiktokMatchRequest, Long> {

    Optional<TiktokMatchRequest> findByRequester(Member requester);

    Optional<TiktokMatchRequest> findByTargetPhoneNumberAndTargetTiktokIdAndMatchedFalseAndStatus(
            String phone,
            String tiktokId,
            MatchStatus status
    );

    boolean existsByRequester(Member requester);
}
