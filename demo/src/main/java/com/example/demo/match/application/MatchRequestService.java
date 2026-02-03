package com.example.demo.match.application;

import com.example.demo.common.util.AESUtil;
import com.example.demo.login.member.domain.member.Member;
import com.example.demo.login.member.infrastructure.member.MemberJpaRepository;
import com.example.demo.match.domain.*;
import com.example.demo.match.dto.MatchRequestCommand;
import com.example.demo.match.dto.MatchResponseDto;
import com.example.demo.match.event.MatchCompletedEvent;
import com.example.demo.match.strategy.MatchScoreStrategy;
import com.example.demo.login.global.exception.exceptions.CustomErrorCode;
import com.example.demo.login.global.exception.exceptions.CustomException;
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

    @Transactional
    public void createMatchRequest(Long memberId, MatchRequestCommand command) {
        Member me = getMember(memberId);

        String inputPhone = command.getTargetPhone().trim();
        String inputInsta = command.getTargetInsta().trim().toLowerCase();

        String encTargetPhone = AESUtil.encrypt(inputPhone);
        String encTargetInsta = AESUtil.encrypt(inputInsta);

        String myEncPhone = me.getPhoneNumber();
        String myEncInsta = me.getInstagramId();

        if (matchRequestRepository.existsByRequester(me)) {
            throw new CustomException(CustomErrorCode.DUPLICATE_MATCH_REQUEST);
        }

        MatchRequest myRequest = MatchRequest.builder()
                .requester(me)
                .targetName(command.getTargetName())
                .targetInstagramId(encTargetInsta)
                .targetPhoneNumber(encTargetPhone)
                .requesterDesire(command.getRequesterDesire())
                .status(MatchStatus.PENDING)
                .matched(false)
                .build();

        matchRequestRepository.save(myRequest);
        log.info("📩 [내 요청 저장 완료] → {}", me.getMemberName());

        Optional<MatchRequest> reverseOpt =
                matchRequestRepository.findByTargetPhoneNumberAndTargetInstagramIdAndMatchedFalseAndStatus(
                        myEncPhone, myEncInsta, MatchStatus.PENDING);

        if (reverseOpt.isPresent()) {
            MatchRequest reverseReq = reverseOpt.get();
            Member opponent = reverseReq.getRequester();

            int myDesire = myRequest.getRequesterDesire();
            int yourDesire = reverseReq.getRequesterDesire();

            MatchMessage message = MatchScoreStrategy.calculate(myDesire, yourDesire);

            reverseReq.setMatched(true);
            reverseReq.setMatchedMember(me);
            reverseReq.setMatchMessage(message);
            reverseReq.setTargetDesire(myDesire);
            reverseReq.setStatus(MatchStatus.MATCHED);

            myRequest.setMatched(true);
            myRequest.setMatchedMember(opponent);
            myRequest.setMatchMessage(message);
            myRequest.setTargetDesire(yourDesire);
            myRequest.setStatus(MatchStatus.MATCHED);

            matchRequestRepository.save(reverseReq);
            matchRequestRepository.save(myRequest);

            log.info("🎯 [쌍방 매칭 성공] {} ❤️ {}", me.getMemberName(), opponent.getMemberName());
            eventPublisher.publishEvent(new MatchCompletedEvent(me, opponent, message));
        } else {
            log.info("⌛ [상대 요청 없음] → 대기 상태 유지");
        }
    }

    @Transactional(readOnly = true)
    public MatchResponseDto getMatchRequest(Long memberId) {
        Member requester = getMember(memberId);

        return matchRequestRepository.findByRequester(requester)
                .map(request -> MatchResponseDto.builder()
                        .targetPhone(AESUtil.decrypt(request.getTargetPhoneNumber()))
                        .targetInsta(AESUtil.decrypt(request.getTargetInstagramId()))
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
    public void updateMatchRequest(Long memberId, MatchRequestCommand command) {
        Member requester = getMember(memberId);
        MatchRequest request = matchRequestRepository.findByRequester(requester)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MATCH_NOT_FOUND));

        if (request.isMatched()) {
            throw new CustomException(CustomErrorCode.MATCH_ALREADY_COMPLETED);
        }

        String phone = command.getTargetPhone().trim();
        String insta = command.getTargetInsta().trim().toLowerCase();

        request.updateTargetInfo(
                AESUtil.encrypt(phone),
                AESUtil.encrypt(insta),
                command.getTargetName(),
                command.getRequesterDesire()
        );
    }

    /**
     * ✅ 매칭 상태와 관계없이 삭제 허용
     */
    @Transactional
    public void deleteMatchRequest(Long memberId) {
        Member requester = getMember(memberId);
        MatchRequest request = matchRequestRepository.findByRequester(requester)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MATCH_NOT_FOUND));

        matchRequestRepository.delete(request);
    }

    @Transactional(readOnly = true)
    public String checkMatchResult(Long memberId) {
        Member requester = getMember(memberId);
        MatchRequest request = matchRequestRepository.findByRequester(requester)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MATCH_NOT_FOUND));

        if (!request.isMatched()) {
            throw new CustomException(CustomErrorCode.MATCH_RESULT_PENDING);
        }

        return "🎊 매칭 결과: " + request.getMatchMessage().getMessage();
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MATCH_MEMBER_NOT_FOUND));
    }
}
