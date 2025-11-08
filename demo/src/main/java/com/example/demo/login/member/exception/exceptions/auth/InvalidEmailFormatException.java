package com.example.demo.login.member.exception.exceptions.auth;

import com.example.demo.login.member.exception.exceptions.MemberErrorCode;
import com.example.demo.login.member.exception.exceptions.MemberException;

public class InvalidEmailFormatException extends MemberException {
    public InvalidEmailFormatException() {
        super(MemberErrorCode.INVALID_EMAIL);
    }
}
