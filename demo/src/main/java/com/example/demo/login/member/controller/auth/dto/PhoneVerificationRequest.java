package com.example.demo.login.member.controller.auth.dto;

public record PhoneVerificationRequest(String phoneNumber, String verificationCode) {
}
