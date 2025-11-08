package com.example.demo.login.member.exception.exceptions.auth;


import com.example.demo.login.member.exception.exceptions.MemberErrorCode;
import com.example.demo.login.member.exception.exceptions.MemberException;

public class DuplicateEmailException extends MemberException {

    public DuplicateEmailException() {
        super(MemberErrorCode.DUPLICATED_EMAIL);
    }
}
