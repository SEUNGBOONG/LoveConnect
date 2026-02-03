package com.example.demo.match.domain;

import com.example.demo.login.member.domain.member.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
public class TiktokMatchRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    private Member requester;

    private String targetPhoneNumber;
    private String targetTiktokId;
    private String targetName;

    private boolean matched;

    @OneToOne(fetch = LAZY)
    private Member matchedMember;

    private int requesterDesire;
    private Integer targetDesire;

    @Enumerated(EnumType.STRING)
    private MatchMessage matchMessage;

    @Enumerated(EnumType.STRING)
    private MatchStatus status;

    /**
     * 매칭 완료 처리
     */
    public MatchCompletedEvent matchWith(TiktokMatchRequest other, MatchMessage message, int targetDesire) {
        validateCanMatch();
        other.validateCanMatch();
        
        this.markMatched(other.getRequester(), message, targetDesire);
        other.markMatched(this.getRequester(), message, this.requesterDesire);

        return new MatchCompletedEvent(this.getRequester(), other.getRequester(), message);
    }
    
    /**
     * 매칭 완료 상태로 변경
     */
    private void markMatched(Member matchedMember, MatchMessage message, int targetDesire) {
        this.matched = true;
        this.matchedMember = matchedMember;
        this.matchMessage = message;
        this.targetDesire = targetDesire;
        this.status = MatchStatus.MATCHED;
    }
    
    /**
     * 매칭 가능 여부 검증
     */
    private void validateCanMatch() {
        if (this.matched) {
            throw new IllegalStateException("이미 매칭 완료된 요청입니다.");
        }
        if (this.status != MatchStatus.PENDING) {
            throw new IllegalStateException("대기 중인 요청만 매칭 가능합니다.");
        }
    }
    
    /**
     * 수정 가능 여부 확인
     */
    public boolean canUpdate() {
        return !matched && status == MatchStatus.PENDING;
    }
    
    /**
     * 삭제 가능 여부 확인
     */
    public boolean canDelete() {
        return true; // 매칭 상태와 관계없이 삭제 허용
    }
    
    /**
     * 대상 정보 업데이트
     */
    public void updateTargetInfo(String phone, String tiktok, String name, int desire) {
        if (!canUpdate()) {
            throw new IllegalStateException("매칭 완료된 요청은 수정할 수 없습니다.");
        }
        this.targetPhoneNumber = phone;
        this.targetTiktokId = tiktok;
        this.targetName = name;
        this.requesterDesire = desire;
    }
    
    /**
     * 매칭 결과 확인 가능 여부
     */
    public boolean hasMatchResult() {
        return matched && matchMessage != null;
    }
}

