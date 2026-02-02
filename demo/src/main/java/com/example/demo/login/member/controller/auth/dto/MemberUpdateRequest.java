package com.example.demo.login.member.controller.auth.dto;

public record MemberUpdateRequest(
        String nickname,
        String instagramId,
        String mbti,
        String tiktokId,
        Boolean emailAgree
) { }
