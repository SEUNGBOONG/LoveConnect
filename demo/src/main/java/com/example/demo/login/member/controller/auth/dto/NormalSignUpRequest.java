package com.example.demo.login.member.controller.auth.dto;

public record NormalSignUpRequest(
        String email,
        String name,
        String password,
        String nickname,
        String phoneNumber,
        String instagramId,
        String tiktokId,
        String mbti,
        String birthYear,
        String birthMonth,
        String birthDay,
        String gender,
        boolean emailAgree,
        boolean privacyAgree,
        boolean useAgree
) {}
