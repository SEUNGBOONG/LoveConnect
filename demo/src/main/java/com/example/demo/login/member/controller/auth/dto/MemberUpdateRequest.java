package com.example.demo.login.member.controller.auth.dto;

public record MemberUpdateRequest(
        String nickname,
        String instagramId,
        String tiktokId,   // 🔥 추가
        String mbti,
        Boolean emailAgree
) {}
