package com.example.demo.match.domain;

import com.example.demo.login.member.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MatchRequestRepository extends JpaRepository<MatchRequest, Long> {

    Optional<MatchRequest> findByRequesterAndChannelType(
            Member requester,
            MatchChannelType channelType
    );

    boolean existsByRequesterAndChannelType(
            Member requester,
            MatchChannelType channelType
    );

    Optional<MatchRequest> findByTargetPhoneNumberAndTargetSocialIdAndChannelTypeAndMatchedFalseAndStatus(
            String phone,
            String socialId,
            MatchChannelType channelType,
            MatchStatus status
    );
}
