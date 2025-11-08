package com.example.demo.login.global.exception.exceptions;

public class InvalidRegistrationNumber extends IllegalArgumentException{
    public InvalidRegistrationNumber() {
        super("유효하지 않은 사업자입니다.");
    }
}
