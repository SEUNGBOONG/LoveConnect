package com.example.demo.match.application;

import com.example.demo.common.util.AESUtil;
import com.example.demo.login.member.domain.member.Member;
import com.example.demo.login.member.infrastructure.member.MemberJpaRepository;
import com.example.demo.match.domain.MatchRequest;
import com.example.demo.match.domain.MatchRequestRepository;
import com.example.demo.match.dto.MatchRequestCommand;
import com.example.demo.match.dto.MatchResponseDto;
import com.example.demo.match.domain.MatchMessage;
import com.example.demo.match.strategy.MatchScoreStrategy;
import com.example.demo.match.domain.MatchStatus;
import com.example.demo.match.event.MatchCompletedEvent;
import com.example.demo.login.global.exception.exceptions.CustomErrorCode;
import com.example.demo.login.global.exception.exceptions.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MatchRequestService {

    private final MatchRequestRepository matchRequestRepository;
    private final MemberJpaRepository memberRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void createMatchRequest(Long memberId, MatchRequestCommand command) {
        Member requester = getMember(memberId);

        if (matchRequestRepository.existsByRequester(requester)) {
            throw new CustomException(CustomErrorCode.MATCH_ALREADY_REQUESTED);
        }

        String encryptedPhone = AESUtil.encrypt(command.getTargetPhone().trim());
        String encryptedInsta = AESUtil.encrypt(command.getTargetInsta().trim());

        MatchRequest newRequest = MatchRequest.builder()
                .requester(requester)
                .targetPhoneNumber(encryptedPhone)
                .targetInstagramId(encryptedInsta)
                .targetName(command.getTargetName())
                .requesterDesire(command.getRequesterDesire())
                .status(MatchStatus.PENDING)
                .matched(false)
                .build();

        matchRequestRepository.save(newRequest);

        // 상대방의 매칭 요청이 이미 존재하는지 확인
        matchRequestRepository.findByTargetPhoneNumberAndTargetInstagramIdAndMatchedFalse(
                requester.getPhoneNumber(), requester.getInstagramId()
        ).ifPresent(oppositeRequest -> handleMatching(newRequest, oppositeRequest));
    }

    private void handleMatching(MatchRequest requesterRequest, MatchRequest targetRequest) {
        int requesterDesire = requesterRequest.getRequesterDesire();
        int targetDesire = targetRequest.getRequesterDesire();

        MatchMessage matchMessage = MatchScoreStrategy.calculate(requesterDesire, targetDesire);

        MatchCompletedEvent event = requesterRequest.matchWith(targetRequest, matchMessage);
        matchRequestRepository.save(requesterRequest);
        matchRequestRepository.save(targetRequest);

        eventPublisher.publishEvent(event);
    }

    @Transactional(readOnly = true)
    public MatchResponseDto getMatchRequest(Long memberId) {
        Member requester = getMember(memberId);
        MatchRequest request = matchRequestRepository.findByRequester(requester)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MATCH_NOT_FOUND));

        return MatchResponseDto.builder()
                .targetPhone(AESUtil.decrypt(request.getTargetPhoneNumber()))
                .targetInsta(AESUtil.decrypt(request.getTargetInstagramId()))
                .targetName(request.getTargetName())
                .requesterDesire(request.getRequesterDesire())
                .matched(request.isMatched())
                .matchMessage(request.getMatchMessage() != null ? request.getMatchMessage().getMessage() : null)
                .build();
    }

    @Transactional
    public void updateMatchRequest(Long memberId, MatchRequestCommand command) {
        Member requester = getMember(memberId);
        MatchRequest request = matchRequestRepository.findByRequester(requester)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MATCH_NOT_FOUND));

        if (request.isMatched()) {
            throw new CustomException(CustomErrorCode.MATCH_ALREADY_COMPLETED);
        }

        String encryptedPhone = AESUtil.encrypt(command.getTargetPhone().trim());
        String encryptedInsta = AESUtil.encrypt(command.getTargetInsta().trim());

        request.updateTargetInfo(
                encryptedPhone,
                encryptedInsta,
                command.getTargetName(),
                command.getRequesterDesire()
        );
    }

    @Transactional
    public void deleteMatchRequest(Long memberId) {
        Member requester = getMember(memberId);
        MatchRequest request = matchRequestRepository.findByRequester(requester)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MATCH_NOT_FOUND));

        if (request.isMatched()) {
            throw new CustomException(CustomErrorCode.MATCH_ALREADY_COMPLETED);
        }

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
