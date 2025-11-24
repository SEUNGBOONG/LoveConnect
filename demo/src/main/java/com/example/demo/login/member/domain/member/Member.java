package com.example.demo.login.member.domain.member;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
@AllArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String memberEmail;

    @Column(nullable = false)
    private String memberName;

    @Column(nullable = false)
    private String memberPassword;

    @Column(nullable = false)
    private String memberNickName;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String instagramId;

    @Column(nullable = false)
    private String mbti;

    @Column(nullable = false)
    private String birthDate; // e.g., "980826"

    @Column(nullable = false)
    private String gender; // "MALE", "FEMALE" 등 ENUM 가능

    @Column(nullable = false)
    private boolean emailAgree;

    @Column(nullable = false)
    private boolean privacyAgree;

    @Column(nullable = false)
    private boolean useAgree;

    public void updatePassword(String newPassword) {
        this.memberPassword = newPassword;
    }
}
