package com.example.demo.login.member.domain.member;

import com.example.demo.common.util.AESUtil;
import jakarta.persistence.*;
import lombok.*;

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

    public void updateProfile(String nickname, String instagramId, String mbti, Boolean emailAgree) {
        this.memberNickName = nickname;
        this.instagramId = AESUtil.encrypt(instagramId);
        this.mbti = mbti;
        this.emailAgree = emailAgree;
    }

    public void changePassword(String newEncodedPassword) {
        this.memberPassword = newEncodedPassword;
    }

    public void updatePassword(String newPassword) {
        this.memberPassword = newPassword;
    }
}
