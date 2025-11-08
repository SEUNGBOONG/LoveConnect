package com.example.demo.login.member.exception.exceptions.auth;


import com.example.demo.login.member.exception.exceptions.MemberErrorCode;
import com.example.demo.login.member.exception.exceptions.MemberException;

public class InvalidLoginRequestException extends MemberException {
    public InvalidLoginRequestException() {
        super(MemberErrorCode.INVALID_LOGIN_REQUEST);
    }
}
