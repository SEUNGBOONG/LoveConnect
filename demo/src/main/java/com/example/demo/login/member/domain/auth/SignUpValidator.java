package com.example.demo.login.member.domain.auth;

import com.example.demo.login.member.controller.auth.dto.LoginRequest;
import com.example.demo.login.member.controller.auth.dto.NormalSignUpRequest;
import com.example.demo.login.member.controller.auth.dto.SignUpRequest;
import com.example.demo.login.member.exception.exceptions.auth.InvalidLoginRequestException;
import com.example.demo.login.member.exception.exceptions.auth.InvalidPasswordFormatException;
import com.example.demo.login.member.exception.exceptions.auth.InvalidSignUpRequestException;
import com.example.demo.login.member.exception.exceptions.auth.InvalidSpecialPasswordException;
import org.springframework.stereotype.Component;

import static org.springframework.util.ObjectUtils.isEmpty;

@Component
public class SignUpValidator {

    public static final String REGEX = ".*[a-zA-Z].*";

    public void normalValidateSignupRequestFormat(NormalSignUpRequest signUpRequest) {
        if (signUpRequest == null ||
                isEmpty(signUpRequest.email()) ||
                isEmpty(signUpRequest.name()) ||
                isEmpty(signUpRequest.password()) ||
                isEmpty(signUpRequest.nickname())) {
            throw new InvalidSignUpRequestException();
        }
    }

    public void checkPasswordLength(String password) {
        if (password.length() < 10) {
            throw new InvalidPasswordFormatException();
        }
    }

    public void checkSpecialLetter(String password){
        boolean hasLetter = password.matches(REGEX);
        boolean hasSpecialChar = password.matches(".*[!@#$%^&*()\\-_=+\\[{\\]};:'\"\\\\|,.<>/?].*");

        if (!hasLetter || !hasSpecialChar) {
            throw new InvalidSpecialPasswordException();
        }
    }

    public void validateLoginRequestFormat(LoginRequest loginRequest) {
        if (loginRequest == null ||
                isEmpty(loginRequest.memberEmail()) ||
                isEmpty(loginRequest.memberPassword())) {
            throw new InvalidLoginRequestException();
        }
    }
}
