package com.example.demo.login.global.exception.exceptions;

public class NotFoundDetail extends CustomException{
    public NotFoundDetail() {
        super(CustomErrorCode.NOT_FOUND_DETAIL);
    }
}
