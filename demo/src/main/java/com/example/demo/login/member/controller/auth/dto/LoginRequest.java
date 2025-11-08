package com.example.demo.login.member.controller.auth.dto;
public record LoginRequest(
        String memberEmail,
        String memberPassword
) {
}
