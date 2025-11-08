package com.example.demo.login.member.exception.exceptionhandler.dto;

public record MemberErrorResponse(
        String customCode,
        String message
) {
}
