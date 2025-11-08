package com.example.demo.login.member.exception.exceptions.auth;


import com.example.demo.login.member.exception.exceptions.MemberErrorCode;
import com.example.demo.login.member.exception.exceptions.MemberException;

public class DuplicateNickNameException extends MemberException {

    public DuplicateNickNameException() {
        super(MemberErrorCode.DUPLICATED_NICK_NAME);
    }
}
