package com.example.demo.login.member.domain.member;

import com.example.demo.common.util.AESUtil;
import com.example.demo.match.domain.MatchChannelType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    /** 🔥 인스타그램 ID (암호화 저장) */
    @Column(nullable = false)
    private String instagramId;

    /** 🔥 틱톡 ID (암호화 저장, 선택값) */
    @Column
    private String tiktokId;

    @Column(nullable = false)
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

    /* =========================
       비즈니스 메서드
       ========================= */

    public void withdraw() {
        this.isDeleted = true;
        this.withdrawnAt = LocalDateTime.now();

        this.memberPassword = null;
        this.phoneNumber = null;
        this.instagramId = null;
        this.tiktokId = null;
        this.memberNickName = "탈퇴한 회원";
    }

    public void updateProfile(
            String nickname,
            String instagramId,
            String tiktokId,
            String mbti,
            Boolean emailAgree
    ) {
        this.memberNickName = nickname;

        if (instagramId != null) {
            this.instagramId = AESUtil.encrypt(instagramId.trim().toLowerCase());
        }

        if (tiktokId != null) {
            this.tiktokId = AESUtil.encrypt(tiktokId.trim().toLowerCase());
        }

        this.mbti = mbti;
        this.emailAgree = emailAgree;
    }

    public void changePassword(String newEncodedPassword) {
        this.memberPassword = newEncodedPassword;
    }

    public void updatePassword(String newPassword) {
        this.memberPassword = newPassword;
    }

    /**
     * 🔥 매칭용 소셜 ID 조회 (채널 기준)
     */
    public String getSocialIdByChannel(MatchChannelType channelType) {
        if (channelType == MatchChannelType.INSTAGRAM) {
            return instagramId;
        }
        if (channelType == MatchChannelType.TIKTOK) {
            return tiktokId;
        }
        throw new IllegalArgumentException("지원하지 않는 매칭 채널입니다.");
    }
}
