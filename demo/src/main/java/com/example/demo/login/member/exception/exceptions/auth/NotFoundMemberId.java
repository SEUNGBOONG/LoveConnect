package com.example.demo.login.member.exception.exceptions.auth;

import com.example.demo.login.member.exception.exceptions.MemberErrorCode;
import com.example.demo.login.member.exception.exceptions.MemberException;

public class NotFoundMemberId extends MemberException {
    public NotFoundMemberId() {
        super(MemberErrorCode.NOT_FOUND_MEMBER_ID);
    }
}
