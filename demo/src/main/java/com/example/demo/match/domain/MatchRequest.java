package com.example.demo.match.domain;

import com.example.demo.login.member.domain.member.Member;
import com.example.demo.match.domain.MatchMessage;
import com.example.demo.match.event.MatchCompletedEvent;
import jakarta.persistence.*;
import lombok.*;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
public class MatchRequest {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    private Member requester;

    private String targetPhoneNumber;
    private String targetInstagramId;
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

    public MatchCompletedEvent matchWith(MatchRequest other, MatchMessage message) {
        this.matched = true;
        this.matchedMember = other.getRequester();
        this.matchMessage = message;
        this.status = MatchStatus.MATCHED;

        other.matched = true;
        other.matchedMember = this.getRequester();
        other.matchMessage = message;
        other.status = MatchStatus.MATCHED;

        return new MatchCompletedEvent(this.getRequester(), other.getRequester(), message);
    }

    // MatchRequest.java

    public void updateTargetInfo(String phone, String insta, String name, int desire) {
        this.targetPhoneNumber = phone;
        this.targetInstagramId = insta;
        this.targetName = name;
        this.requesterDesire = desire;
    }
}
