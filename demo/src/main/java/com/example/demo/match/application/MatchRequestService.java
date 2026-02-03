package com.example.demo.match.application;

import com.example.demo.common.util.AESUtil;
import com.example.demo.login.global.exception.exceptions.CustomErrorCode;
import com.example.demo.login.global.exception.exceptions.CustomException;
import com.example.demo.login.member.domain.member.Member;
import com.example.demo.login.member.infrastructure.member.MemberJpaRepository;
import com.example.demo.match.domain.MatchMessage;
import com.example.demo.match.domain.MatchRequest;
import com.example.demo.match.domain.MatchRequestRepository;
import com.example.demo.match.domain.MatchStatus;
import com.example.demo.match.domain.value.PhoneNumber;
import com.example.demo.match.domain.value.SocialId;
import com.example.demo.match.dto.MatchRequestCommand;
import com.example.demo.match.dto.MatchResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
public class MatchRequestService extends MatchRequestServiceTemplate<MatchRequest> {

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
                PhoneNumber.fromRaw(AESUtil.decrypt(requesterPhone)),
                SocialId.fromRaw(AESUtil.decrypt(requesterSocialId)),
                MatchStatus.PENDING
        );
    }
    
    @Override
    protected MatchRequest createNewRequest(Member requester, Object command) {
        MatchRequestCommand cmd = (MatchRequestCommand) command;
        
        return MatchRequest.builder()
                .requester(requester)
                .targetName(cmd.getTargetName())
                .targetPhoneNumber(PhoneNumber.fromRaw(cmd.getTargetPhone()))
                .targetInstagramId(SocialId.fromRaw(cmd.getTargetInsta()))
                .requesterDesire(cmd.getRequesterDesire())
                .status(MatchStatus.PENDING)
                .matched(false)
                .build();
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
    
    @Override
    protected boolean existsByRequester(Member requester) {
        return matchRequestRepository.existsByRequester(requester);
    }
    
    @Override
    protected void completeMatch(
            MatchRequest myRequest,
            MatchRequest reverseRequest,
            Member me,
            Member opponent,
            MatchMessage message
    ) {
        myRequest.matchWith(reverseRequest, message);
        matchRequestRepository.save(myRequest);
        matchRequestRepository.save(reverseRequest);
    }
    
    @Override
    protected int getRequesterDesire(MatchRequest request) {
        return request.getRequesterDesire();
    }

    @Transactional(readOnly = true)
    public MatchResponseDto getMatchRequest(Long memberId) {
        Member requester = getMember(memberId);

        return matchRequestRepository.findByRequester(requester)
                .map(request -> MatchResponseDto.builder()
                        .targetPhone(request.getTargetPhoneNumber().decrypt())
                        .targetInsta(request.getTargetInstagramId().decrypt())
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

        request.updateTarget(
                PhoneNumber.fromRaw(command.getTargetPhone()),
                SocialId.fromRaw(command.getTargetInsta()),
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
