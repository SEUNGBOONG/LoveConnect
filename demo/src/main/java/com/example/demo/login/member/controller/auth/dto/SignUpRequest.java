package com.example.demo.login.member.controller.auth.dto;

public record SignUpRequest(
        String memberEmail,
        String memberName,
        String memberPassword,
        String memberNickName,
        String roadAddress,
        String jibunAddress,
        String zipCode,
        boolean checkCorporation,
        String corporationNumber,
        String phoneNumber
) {
}
