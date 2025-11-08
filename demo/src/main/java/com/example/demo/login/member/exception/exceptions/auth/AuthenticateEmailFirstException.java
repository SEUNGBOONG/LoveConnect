package com.example.demo.login.member.exception.exceptions.auth;

import com.example.demo.login.member.exception.exceptions.MemberErrorCode;
import com.example.demo.login.member.exception.exceptions.MemberException;

public class AuthenticateEmailFirstException extends MemberException {

    public AuthenticateEmailFirstException() {
        super(MemberErrorCode.AUTHENTICATE_EMAIL_FIRST);
    }
}
