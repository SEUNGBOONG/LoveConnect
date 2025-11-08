package com.example.demo.login.global.exception.exceptionhandler.dto;

public record ErrorResponse(
        String customCode,
        String message
) {
}
