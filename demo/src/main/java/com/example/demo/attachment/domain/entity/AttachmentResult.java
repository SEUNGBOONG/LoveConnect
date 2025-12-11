package com.example.demo.attachment.domain.entity;

import com.example.demo.login.member.domain.member.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttachmentResult {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    private String resultType; // 안정형/불안형/회피형/공포회피형
    private double anxiousScore;
    private double avoidantScore;

    private String createdAt;
}
