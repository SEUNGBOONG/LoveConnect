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

    @Column(nullable = true)
    private String instagramId;

    @Column
    private String tiktokId;

    @Column(nullable = true)
    private String mbti;

    @Column(nullable = false)
    private String birthDate;

    @Column(nullable = false)
    private String gender;

    @Column(nullable = false)
    private boolean emailAgree;

    @Column(nullable = false)
    private boolean privacyAgree;

    @Column(nullable = false)
    private boolean useAgree;

    @Column(nullable = false)
    private boolean isDeleted = false;

    @Column(name = "toss_ci", unique = true)
    private String tossCi;

    @Column(unique = true) // ✅ 추가
    private Long userKey;

    @Column
    private LocalDateTime withdrawnAt;

    public void withdraw() {
        this.isDeleted = true;
        this.withdrawnAt = LocalDateTime.now();

        this.memberPassword = null;
        this.phoneNumber = null;
        this.instagramId = null;
        this.memberNickName = "탈퇴한 회원";
    }

    public void updateTiktokId(String encryptedTiktokId) {
        this.tiktokId = encryptedTiktokId;
    }

    public void updateProfile(
            String nickname,
            String instagramId,
            String tiktokId,
            String mbti,
            Boolean emailAgree
    ) {
        if (nickname != null) {
            this.memberNickName = nickname;
        }
        this.instagramId = instagramId;
        this.tiktokId = tiktokId;

        if (mbti != null) {
            this.mbti = mbti;
        }

        if (emailAgree != null) {
            this.emailAgree = emailAgree;
        }
    }

    public void disconnectToss() {
        this.tossCi = null;
        this.userKey = null;
    }

    public void changePassword(String newEncodedPassword) {
        this.memberPassword = newEncodedPassword;
    }

    public void updateTossProfile(
            String nickname,
            String instagramId,
            String tiktokId,
            String mbti
    ) {
        if (nickname != null && !nickname.isBlank()) {
            this.memberNickName = nickname;
        }

        // null 허용 — 입력값 그대로 반영
        this.instagramId = instagramId;
        this.tiktokId = tiktokId;

        if (mbti != null) {
            this.mbti = mbti;
        }
    }
    public void setTossCi(String tossCi) {
        this.tossCi = tossCi;
    }

    public void setUserKey(Long userKey) {
        this.userKey = userKey;
    }
}
