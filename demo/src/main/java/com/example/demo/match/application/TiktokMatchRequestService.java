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
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
public class TiktokMatchRequestService extends AbstractMatchRequestService<TiktokMatchRequest> {

    private final TiktokMatchRequestRepository tiktokMatchRequestRepository;

    public TiktokMatchRequestService(
            TiktokMatchRequestRepository tiktokMatchRequestRepository,
            MemberJpaRepository memberRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        super(memberRepository, eventPublisher);
        this.tiktokMatchRequestRepository = tiktokMatchRequestRepository;
    }

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
        
        processMatchRequest(memberId, command);
    }
    
    @Override
    protected Optional<TiktokMatchRequest> findReverseRequest(
            Member requester, 
            String requesterPhone, 
            String requesterSocialId
    ) {
        return tiktokMatchRequestRepository
                .findByTargetPhoneNumberAndTargetTiktokIdAndMatchedFalseAndStatus(
                        requesterPhone,
                        requesterSocialId,
                        MatchStatus.PENDING
                );
    }
    
    @Override
    protected TiktokMatchRequest createNewRequest(Member requester, Object command) {
        TiktokMatchRequestCommand cmd = (TiktokMatchRequestCommand) command;
        
        String encTargetPhone = AESUtil.encrypt(cmd.getTargetPhone().trim());
        String encTargetTiktok = AESUtil.encrypt(cmd.getTargetTiktok().trim().toLowerCase());
        
        return TiktokMatchRequest.builder()
                .requester(requester)
                .targetName(cmd.getTargetName())
                .targetTiktokId(encTargetTiktok)
                .targetPhoneNumber(encTargetPhone)
                .requesterDesire(cmd.getRequesterDesire())
                .status(MatchStatus.PENDING)
                .matched(false)
                .build();
    }
    
    @Override
    protected MatchCompletedEvent completeMatch(
            TiktokMatchRequest myRequest, 
            TiktokMatchRequest reverseRequest, 
            Member me, 
            Member opponent
    ) {
        int myDesire = myRequest.getRequesterDesire();
        int opponentDesire = reverseRequest.getRequesterDesire();
        
        MatchMessage message = calculateMatchMessage(myDesire, opponentDesire);
        
        MatchCompletedEvent event = myRequest.matchWith(reverseRequest, message, opponentDesire);
        
        tiktokMatchRequestRepository.save(myRequest);
        tiktokMatchRequestRepository.save(reverseRequest);
        
        return event;
    }
    
    @Override
    protected boolean existsByRequester(Member requester) {
        return tiktokMatchRequestRepository.existsByRequester(requester);
    }
    
    @Override
    protected void saveRequest(TiktokMatchRequest request) {
        tiktokMatchRequestRepository.save(request);
    }
    
    @Override
    protected String getRequesterSocialId(Member member) {
        return member.getTiktokId();
    }
    
    @Override
    protected Member getOpponentFromRequest(TiktokMatchRequest request) {
        return request.getRequester();
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

        if (!request.canUpdate()) {
            throw new CustomException(CustomErrorCode.MATCH_ALREADY_COMPLETED);
        }

        request.updateTargetInfo(
                AESUtil.encrypt(command.getTargetPhone().trim()),
                AESUtil.encrypt(command.getTargetTiktok().trim().toLowerCase()),
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

        if (!request.canDelete()) {
            throw new IllegalStateException("삭제할 수 없는 요청입니다.");
        }

        tiktokMatchRequestRepository.delete(request);
    }
}
