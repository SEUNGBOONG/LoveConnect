package com.example.demo.login.member.domain.member;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity(name = "member")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String memberEmail;

    @Column(nullable = false)
    private String memberName;

    @Column(nullable = false)
    private String memberPassword;

    @Column(nullable = false, unique = true)
    private String memberNickName;

    @Column(nullable = false)
    private String phoneNumber;

    private String instagramId;
    private String tiktokId;
    private String mbti;

    @Column(nullable = false)
    private boolean emailAgree;

    @Column(nullable = false)
    private boolean isDeleted = false;

    private LocalDateTime withdrawnAt;

    /* ===================== 탈퇴 ===================== */

    public void withdraw() {
        this.isDeleted = true;
        this.withdrawnAt = LocalDateTime.now();

        this.memberPassword = null;
        this.phoneNumber = null;
        this.instagramId = null;
        this.tiktokId = null;
        this.memberNickName = "탈퇴한 회원";
    }

    /* ===================== 프로필 ===================== */

    public void updateProfile(
            String nickname,
            String instagramId,
            String tiktokId,
            String mbti,
            Boolean emailAgree
    ) {
        if (nickname != null) this.memberNickName = nickname;
        this.instagramId = instagramId;
        this.tiktokId = tiktokId;
        if (mbti != null) this.mbti = mbti;
        if (emailAgree != null) this.emailAgree = emailAgree;
    }
}
