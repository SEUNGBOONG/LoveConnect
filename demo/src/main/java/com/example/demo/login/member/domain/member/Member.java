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

    @Column(nullable = false)
    private String instagramId;

    @Column
    private String tiktokId;

    @Column
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

    @Column(unique = true)
    private Long userKey;

    @Column
    private LocalDateTime withdrawnAt;

    public void withdraw() {
        this.isDeleted = true;
        this.withdrawnAt = LocalDateTime.now();

        // 고유값 기반으로 유니크 회피
        String suffix = "_deleted_" + this.id;

        this.memberEmail = "deleted" + suffix + "@user.com";
        this.memberNickName = "탈퇴한회원" + suffix;
        this.phoneNumber = "deleted" + suffix;
        this.instagramId = "deleted" + suffix;
        this.tiktokId = "deleted" + suffix;
        this.memberPassword = "WITHDRAWN_USER_PASSWORD";

        this.tossCi = null;
        this.userKey = null;
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
        if (nickname != null) this.memberNickName = nickname;
        this.instagramId = instagramId;
        this.tiktokId = tiktokId;
        if (mbti != null) this.mbti = mbti;
        if (emailAgree != null) this.emailAgree = emailAgree;
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

    // 추가로 필요 시 getter 메서드도 명시 가능
    public String getGender() {
        return gender;
    }

    public String getBirthDate() {
        return birthDate;
    }
}
