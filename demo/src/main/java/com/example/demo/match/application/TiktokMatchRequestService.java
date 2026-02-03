package com.example.demo.match.application;

import com.example.demo.common.util.AESUtil;
import com.example.demo.login.member.domain.member.Member;
import com.example.demo.login.member.infrastructure.member.MemberJpaRepository;
import com.example.demo.login.global.exception.exceptions.CustomErrorCode;
import com.example.demo.login.global.exception.exceptions.CustomException;
import com.example.demo.match.domain.*;
import com.example.demo.match.dto.TiktokMatchRequestCommand;
import com.example.demo.match.dto.TiktokMatchResponseDto;
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
public class TiktokMatchRequestService {

    private final TiktokMatchRequestRepository tiktokMatchRequestRepository;
    private final MemberJpaRepository memberRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void createMatchRequest(Long memberId, TiktokMatchRequestCommand command) {
        Member me = getMember(memberId);

        // ✅ 1. 내 TikTok ID 필수
        if (me.getTiktokId() == null) {
            throw new CustomException(CustomErrorCode.TIKTOK_ID_REQUIRED);
        }

        // ✅ 2. 상대 TikTok ID 필수
        if (command.getTargetTiktok() == null || command.getTargetTiktok().isBlank()) {
            throw new CustomException(CustomErrorCode.TARGET_TIKTOK_REQUIRED);
        }

        String inputPhone = command.getTargetPhone().trim();
        String inputTiktok = command.getTargetTiktok().trim().toLowerCase();

        String encTargetPhone = AESUtil.encrypt(inputPhone);
        String encTargetTiktok = AESUtil.encrypt(inputTiktok);

        String myEncPhone = me.getPhoneNumber();
        String myEncTiktok = me.getTiktokId();

        if (tiktokMatchRequestRepository.existsByRequester(me)) {
            throw new CustomException(CustomErrorCode.DUPLICATE_MATCH_REQUEST);
        }

        TiktokMatchRequest myRequest = TiktokMatchRequest.builder()
                .requester(me)
                .targetName(command.getTargetName())
                .targetTiktokId(encTargetTiktok)
                .targetPhoneNumber(encTargetPhone)
                .requesterDesire(command.getRequesterDesire())
                .status(MatchStatus.PENDING)
                .matched(false)
                .build();

        tiktokMatchRequestRepository.save(myRequest);

        Optional<TiktokMatchRequest> reverseOpt =
                tiktokMatchRequestRepository
                        .findByTargetPhoneNumberAndTargetTiktokIdAndMatchedFalseAndStatus(
                                myEncPhone,
                                myEncTiktok,
                                MatchStatus.PENDING
                        );

        if (reverseOpt.isPresent()) {
            TiktokMatchRequest reverseReq = reverseOpt.get();
            Member opponent = reverseReq.getRequester();

            MatchMessage message = MatchScoreStrategy.calculate(
                    myRequest.getRequesterDesire(),
                    reverseReq.getRequesterDesire()
            );

            myRequest.setMatched(true);
            myRequest.setMatchedMember(opponent);
            myRequest.setTargetDesire(reverseReq.getRequesterDesire());
            myRequest.setMatchMessage(message);
            myRequest.setStatus(MatchStatus.MATCHED);

            reverseReq.setMatched(true);
            reverseReq.setMatchedMember(me);
            reverseReq.setTargetDesire(myRequest.getRequesterDesire());
            reverseReq.setMatchMessage(message);
            reverseReq.setStatus(MatchStatus.MATCHED);

            tiktokMatchRequestRepository.save(myRequest);
            tiktokMatchRequestRepository.save(reverseReq);

            eventPublisher.publishEvent(
                    new MatchCompletedEvent(me, opponent, message)
            );
        }
    }

    @Transactional(readOnly = true)
    public TiktokMatchResponseDto getMatchRequest(Long memberId) {
        Member requester = getMember(memberId);

        return tiktokMatchRequestRepository.findByRequester(requester)
                .map(request -> TiktokMatchResponseDto.builder()
                        .targetPhone(AESUtil.decrypt(request.getTargetPhoneNumber()))
                        .targetTiktok(AESUtil.decrypt(request.getTargetTiktokId()))
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

    @Transactional
    public void updateMatchRequest(Long memberId, TiktokMatchRequestCommand command) {
        Member requester = getMember(memberId);
        TiktokMatchRequest request = tiktokMatchRequestRepository
                .findByRequester(requester)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MATCH_NOT_FOUND));

        if (request.isMatched()) {
            throw new CustomException(CustomErrorCode.MATCH_ALREADY_COMPLETED);
        }

        String phone = command.getTargetPhone();
        String tiktok = command.getTargetTiktok();

        String encryptedPhone = phone == null || phone.isBlank()
                ? null
                : AESUtil.encrypt(phone.trim());

        String encryptedTiktok = tiktok == null || tiktok.isBlank()
                ? null
                : AESUtil.encrypt(tiktok.trim().toLowerCase());

        request.updateTargetInfo(
                encryptedPhone,
                encryptedTiktok,
                command.getTargetName(),
                command.getRequesterDesire()
        );
    }

    @Transactional
    public void deleteMatchRequest(Long memberId) {
        Member requester = getMember(memberId);
        TiktokMatchRequest request = tiktokMatchRequestRepository
                .findByRequester(requester)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MATCH_NOT_FOUND));

        tiktokMatchRequestRepository.delete(request);
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MATCH_MEMBER_NOT_FOUND));
    }
}
