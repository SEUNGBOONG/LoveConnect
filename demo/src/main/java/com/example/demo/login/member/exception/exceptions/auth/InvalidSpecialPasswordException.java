package com.example.demo.login.member.exception.exceptions.auth;

import com.example.demo.login.member.exception.exceptions.MemberErrorCode;
import com.example.demo.login.member.exception.exceptions.MemberException;

public class InvalidSpecialPasswordException extends MemberException {

    public InvalidSpecialPasswordException() {
        super(MemberErrorCode.INVALID_SPECIAL_PASSWORD);
    }
}
