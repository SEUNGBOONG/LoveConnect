package com.example.demo.login.toss.dto.request;

public record TossAdditionalInfoRequest(
        String nickname,
        String instagramId,
        String mbti,
        String tiktokId, // 선택사항
        Boolean emailAgree
) {}
