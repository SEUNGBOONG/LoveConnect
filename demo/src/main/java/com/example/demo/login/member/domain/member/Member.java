package com.example.demo.login.member.domain.member;

import com.example.demo.common.util.AESUtil;
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

    @Column
    private LocalDateTime withdrawnAt;

    public void withdraw() {
        this.isDeleted = true;
        this.withdrawnAt = LocalDateTime.now();

        // 민감 정보 비우기 (선택)
        this.memberPassword = null;
        this.phoneNumber = null;
        this.instagramId = null;
        this.memberNickName = "탈퇴한 회원";
    }

    // Member.java
    public void updateTiktokId(String encryptedTiktokId) {
        this.tiktokId = encryptedTiktokId;
    }

    public void updateProfile(String nickname, String instagramId, String tiktokId, String mbti, Boolean emailAgree) {
        this.memberNickName = nickname;
        this.instagramId = AESUtil.encrypt(instagramId);
        this.mbti = mbti;
        this.tiktokId = AESUtil.encrypt(tiktokId);
        this.emailAgree = emailAgree;
    }

    public void changePassword(String newEncodedPassword) {
        this.memberPassword = newEncodedPassword;
    }

    public void updatePassword(String newPassword) {
        this.memberPassword = newPassword;
    }
}
