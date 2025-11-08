package com.example.demo.login.global.exception.exceptionhandler;

import com.example.demo.common.exception.ApiResponse;
import com.example.demo.login.global.exception.exceptions.CustomErrorCode;
import com.example.demo.login.global.exception.exceptions.CustomException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

// ✅ 이거 하나만 남기세요. 나머지 제거!

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiResponseException(CustomException e) {
        CustomErrorCode errorCode = e.getCustomErrorCode();
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.fail(errorCode.getCustomCode(), errorCode.getMessage()));
    }
}
