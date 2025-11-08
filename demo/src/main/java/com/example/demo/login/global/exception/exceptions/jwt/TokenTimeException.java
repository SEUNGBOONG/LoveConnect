package com.example.demo.login.global.exception.exceptions.jwt;


import com.example.demo.login.global.exception.exceptions.CustomErrorCode;
import com.example.demo.login.global.exception.exceptions.CustomException;

public class TokenTimeException extends CustomException {

    public TokenTimeException() {
        super(CustomErrorCode.EXPIRED_TOKEN);
    }
}
