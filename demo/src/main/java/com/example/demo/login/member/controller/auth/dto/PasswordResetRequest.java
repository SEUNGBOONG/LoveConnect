package com.example.demo.login.member.controller.auth.dto;

public record PasswordResetRequest(
        String phoneNumber,
        String newPassword
) { }
