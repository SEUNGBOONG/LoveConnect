package com.example.demo.match.domain;

import com.example.demo.login.member.domain.member.Member;
import jakarta.persistence.*;
import lombok.*;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    private Member requester;

    private String targetPhoneNumber;

    /** 인스타 ID 또는 틱톡 ID */
    private String targetSocialId;

    @Enumerated(EnumType.STRING)
    private MatchChannelType channelType;

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

    public void updateTargetInfo(
            String phone,
            String socialId,
            String name,
            int desire
    ) {
        this.targetPhoneNumber = phone;
        this.targetSocialId = socialId;
        this.targetName = name;
        this.requesterDesire = desire;
    }
}
