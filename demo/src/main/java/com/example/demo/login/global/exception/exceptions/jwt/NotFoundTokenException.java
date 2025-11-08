package com.example.demo.login.global.exception.exceptions.jwt;


import com.example.demo.login.global.exception.exceptions.CustomErrorCode;
import com.example.demo.login.global.exception.exceptions.CustomException;

public class NotFoundTokenException extends CustomException {

    public NotFoundTokenException() {
        super(CustomErrorCode.NOT_FIND_TOKEN);
    }
}
