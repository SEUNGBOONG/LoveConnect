package com.example.demo.login.member.controller.auth.dto;

import lombok.Data;

@Data
public class ChangePasswordRequest {
    private String email;
    private String newPassword;
    private String newPasswordConfirm;
}
