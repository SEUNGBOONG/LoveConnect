package com.example.demo.login.email.domain;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class EmailCode {

    public String createCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

}
