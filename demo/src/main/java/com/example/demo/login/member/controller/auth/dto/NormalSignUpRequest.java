package com.example.demo.login.member.controller.auth.dto;

public record NormalSignUpRequest(
        String email,
        String name,
        String password,
        String nickname,
        String phoneNumber,
        String instagramId,
        String mbti,
        String birthDate
) {}
