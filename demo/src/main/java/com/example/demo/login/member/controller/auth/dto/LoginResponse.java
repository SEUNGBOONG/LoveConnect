package com.example.demo.login.member.controller.auth.dto;

public record LoginResponse(
        Long memberId,
        String memberName,
        String memberNickName,
        String tiktokId
) {}
