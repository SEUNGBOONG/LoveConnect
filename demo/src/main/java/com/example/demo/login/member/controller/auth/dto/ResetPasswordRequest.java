package com.example.demo.login.member.controller.auth.dto;

import lombok.Getter;

@Getter
public class ResetPasswordRequest {
    private String email;
    private String phoneNumber;
}
