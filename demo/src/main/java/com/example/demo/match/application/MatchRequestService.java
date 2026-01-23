package com.example.demo.match.application;

import com.example.demo.common.util.AESUtil;
import com.example.demo.login.global.exception.exceptions.CustomErrorCode;
import com.example.demo.login.global.exception.exceptions.CustomException;
import com.example.demo.login.member.domain.member.Member;
import com.example.demo.login.member.infrastructure.member.MemberJpaRepository;
import com.example.demo.match.domain.MatchChannelType;
import com.example.demo.match.domain.MatchMessage;
import com.example.demo.match.domain.MatchRequest;
import com.example.demo.match.domain.MatchRequestRepository;
import com.example.demo.match.domain.MatchStatus;
import com.example.demo.match.dto.MatchRequestCommand;
import com.example.demo.match.dto.MatchResponseDto;
import com.example.demo.match.event.MatchCompletedEvent;
import com.example.demo.match.strategy.MatchScoreStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchRequestService {

    private final MatchRequestRepository matchRequestRepository;
    private final MemberJpaRepository memberRepository;
    private final ApplicationEventPublisher eventPublisher;

    /* ===============================
       CREATE
       =============================== */

    @Transactional
    public void createMatchRequest(
            Long memberId,
            MatchRequestCommand command,
            MatchChannelType channelType
    ) {
        Member me = getMember(memberId);

        if (matchRequestRepository.existsByRequesterAndChannelType(me, channelType)) {
            throw new CustomException(CustomErrorCode.DUPLICATE_MATCH_REQUEST);
        }

        String encTargetPhone =
                AESUtil.encrypt(command.getTargetPhone().trim());

        String encTargetSocial =
                AESUtil.encrypt(command.getTargetSocialId().trim().toLowerCase());

        MatchRequest myRequest = MatchRequest.builder()
                .requester(me)
                .targetName(command.getTargetName())
                .targetPhoneNumber(encTargetPhone)
                .targetSocialId(encTargetSocial)
                .channelType(channelType)
                .requesterDesire(command.getRequesterDesire())
                .matched(false)
                .status(MatchStatus.PENDING)
                .build();

        matchRequestRepository.save(myRequest);

        String myPhone = me.getPhoneNumber();
        String mySocialId = me.getSocialIdByChannel(channelType);

        Optional<MatchRequest> reverseOpt =
                matchRequestRepository
                        .findByTargetPhoneNumberAndTargetSocialIdAndChannelTypeAndMatchedFalseAndStatus(
                                myPhone,
                                mySocialId,
                                channelType,
                                MatchStatus.PENDING
                        );

        reverseOpt.ifPresent(matchRequest -> match(matchRequest, myRequest, me));
    }

    /* ===============================
       GET
       =============================== */

    @Transactional(readOnly = true)
    public MatchResponseDto getMatchRequest(
            Long memberId,
            MatchChannelType channelType
    ) {
        Member requester = getMember(memberId);

        return matchRequestRepository
                .findByRequesterAndChannelType(requester, channelType)
                .map(request -> MatchResponseDto.builder()
                        .targetPhone(
                                AESUtil.decrypt(request.getTargetPhoneNumber())
                        )
                        .targetSocialId(
                                AESUtil.decrypt(request.getTargetSocialId())
                        )
                        .targetName(request.getTargetName())
                        .requesterDesire(request.getRequesterDesire())
                        .matched(request.isMatched())
                        .matchMessage(
                                request.getMatchMessage() != null
                                        ? request.getMatchMessage().getMessage()
                                        : null
                        )
                        .build()
                )
                .orElse(null);
    }

    /* ===============================
       UPDATE
       =============================== */

    @Transactional
    public void updateMatchRequest(
            Long memberId,
            MatchRequestCommand command,
            MatchChannelType channelType
    ) {
        Member requester = getMember(memberId);

        MatchRequest request =
                matchRequestRepository
                        .findByRequesterAndChannelType(requester, channelType)
                        .orElseThrow(() ->
                                new CustomException(CustomErrorCode.MATCH_NOT_FOUND)
                        );

        if (request.isMatched()) {
            throw new CustomException(CustomErrorCode.MATCH_ALREADY_COMPLETED);
        }

        request.updateTargetInfo(
                AESUtil.encrypt(command.getTargetPhone().trim()),
                AESUtil.encrypt(command.getTargetSocialId().trim().toLowerCase()),
                command.getTargetName(),
                command.getRequesterDesire()
        );
    }

    /* ===============================
       DELETE
       =============================== */

    @Transactional
    public void deleteMatchRequest(
            Long memberId,
            MatchChannelType channelType
    ) {
        Member requester = getMember(memberId);

        MatchRequest request =
                matchRequestRepository
                        .findByRequesterAndChannelType(requester, channelType)
                        .orElseThrow(() ->
                                new CustomException(CustomErrorCode.MATCH_NOT_FOUND)
                        );

        matchRequestRepository.delete(request);
    }

    /* ===============================
       MATCH LOGIC
       =============================== */

    private void match(
            MatchRequest reverseReq,
            MatchRequest myRequest,
            Member me
    ) {
        Member opponent = reverseReq.getRequester();

        int myDesire = myRequest.getRequesterDesire();
        int yourDesire = reverseReq.getRequesterDesire();

        MatchMessage message =
                MatchScoreStrategy.calculate(myDesire, yourDesire);

        reverseReq.setMatched(true);
        reverseReq.setMatchedMember(me);
        reverseReq.setTargetDesire(myDesire);
        reverseReq.setMatchMessage(message);
        reverseReq.setStatus(MatchStatus.MATCHED);

        myRequest.setMatched(true);
        myRequest.setMatchedMember(opponent);
        myRequest.setTargetDesire(yourDesire);
        myRequest.setMatchMessage(message);
        myRequest.setStatus(MatchStatus.MATCHED);

        matchRequestRepository.save(reverseReq);
        matchRequestRepository.save(myRequest);

        eventPublisher.publishEvent(
                new MatchCompletedEvent(me, opponent, message)
        );
    }

    @Transactional(readOnly = true)
    public String checkMatchResult(
            Long memberId,
            MatchChannelType channelType
    ) {
        Member requester = getMember(memberId);

        MatchRequest request =
                matchRequestRepository
                        .findByRequesterAndChannelType(requester, channelType)
                        .orElseThrow(() ->
                                new CustomException(CustomErrorCode.MATCH_NOT_FOUND)
                        );

        if (!request.isMatched()) {
            throw new CustomException(CustomErrorCode.MATCH_RESULT_PENDING);
        }

        return "🎊 매칭 결과: " + request.getMatchMessage().getMessage();
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(
                        () -> new CustomException(
                                CustomErrorCode.MATCH_MEMBER_NOT_FOUND
                        )
                );
    }
}
