package com.example.demo.match.application;

import com.example.demo.common.util.AESUtil;
import com.example.demo.login.member.domain.member.Member;
import com.example.demo.login.member.infrastructure.member.MemberJpaRepository;
import com.example.demo.match.domain.*;
import com.example.demo.match.dto.MatchRequestCommand;
import com.example.demo.match.dto.MatchResponseDto;
import com.example.demo.match.event.MatchCompletedEvent;
import com.example.demo.login.global.exception.exceptions.CustomErrorCode;
import com.example.demo.login.global.exception.exceptions.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
public class MatchRequestService extends AbstractMatchRequestService<MatchRequest> {

    private final MatchRequestRepository matchRequestRepository;

    public MatchRequestService(
            MatchRequestRepository matchRequestRepository,
            MemberJpaRepository memberRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        super(memberRepository, eventPublisher);
        this.matchRequestRepository = matchRequestRepository;
    }

    @Transactional
    public void createMatchRequest(Long memberId, MatchRequestCommand command) {
        processMatchRequest(memberId, command);
    }
    
    @Override
    protected Optional<MatchRequest> findReverseRequest(
            Member requester, 
            String requesterPhone, 
            String requesterSocialId
    ) {
        return matchRequestRepository.findByTargetPhoneNumberAndTargetInstagramIdAndMatchedFalseAndStatus(
                requesterPhone, requesterSocialId, MatchStatus.PENDING);
    }
    
    @Override
    protected MatchRequest createNewRequest(Member requester, Object command) {
        MatchRequestCommand cmd = (MatchRequestCommand) command;
        
        String encTargetPhone = AESUtil.encrypt(cmd.getTargetPhone().trim());
        String encTargetInsta = AESUtil.encrypt(cmd.getTargetInsta().trim().toLowerCase());
        
        return MatchRequest.builder()
                .requester(requester)
                .targetName(cmd.getTargetName())
                .targetInstagramId(encTargetInsta)
                .targetPhoneNumber(encTargetPhone)
                .requesterDesire(cmd.getRequesterDesire())
                .status(MatchStatus.PENDING)
                .matched(false)
                .build();
    }
    
    @Override
    protected MatchCompletedEvent completeMatch(
            MatchRequest myRequest, 
            MatchRequest reverseRequest, 
            Member me, 
            Member opponent
    ) {
        int myDesire = myRequest.getRequesterDesire();
        int opponentDesire = reverseRequest.getRequesterDesire();
        
        MatchMessage message = calculateMatchMessage(myDesire, opponentDesire);
        
        MatchCompletedEvent event = myRequest.matchWith(reverseRequest, message, opponentDesire);
        
        matchRequestRepository.save(reverseRequest);
        matchRequestRepository.save(myRequest);
        
        return event;
    }
    
    @Override
    protected boolean existsByRequester(Member requester) {
        return matchRequestRepository.existsByRequester(requester);
    }
    
    @Override
    protected void saveRequest(MatchRequest request) {
        matchRequestRepository.save(request);
    }
    
    @Override
    protected String getRequesterSocialId(Member member) {
        return member.getInstagramId();
    }
    
    @Override
    protected Member getOpponentFromRequest(MatchRequest request) {
        return request.getRequester();
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

        if (!request.canUpdate()) {
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

        if (!request.hasMatchResult()) {
            throw new CustomException(CustomErrorCode.MATCH_RESULT_PENDING);
        }

        return "🎊 매칭 결과: " + request.getMatchMessage().getMessage();
    }
}
