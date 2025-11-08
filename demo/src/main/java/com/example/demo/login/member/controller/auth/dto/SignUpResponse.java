package com.example.demo.login.member.controller.auth.dto;

public record SignUpResponse(
        Long id,
        String memberName,
        String memberEmail,
        String memberNickname
) {
}
