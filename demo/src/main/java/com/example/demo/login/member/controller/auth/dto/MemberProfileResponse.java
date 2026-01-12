package com.example.demo.login.member.controller.auth.dto;

public record MemberProfileResponse(
        String email,
        String name,
        String nickname,
        String phoneNumber,
        String instagramId,
        String mbti,
        String gender,
        String birthDate
) { }
