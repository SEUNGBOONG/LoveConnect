package com.example.demo.login.member.exception.exceptionhandler;

import com.example.demo.common.exception.ApiResponse;
import com.example.demo.login.member.exception.exceptionhandler.dto.MemberErrorResponse;
import com.example.demo.login.member.exception.exceptions.MemberErrorCode;
import com.example.demo.login.member.exception.exceptions.MemberException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class MemberExceptionHandler {

    @ExceptionHandler(MemberException.class)
    public ResponseEntity<ApiResponse<Void>> handleMemberException(MemberException e) {
        MemberErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.fail(errorCode.getCustomCode(), errorCode.getMessage()));
    }
}
