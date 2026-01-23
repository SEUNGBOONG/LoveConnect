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

    // MatchRequest.java

    public void updateTargetInfo(String phone, String tiktok, String name, int desire) {
        this.targetPhoneNumber = phone;
        this.targetTiktokId = tiktok;
        this.targetName = name;
        this.requesterDesire = desire;
    }
}

