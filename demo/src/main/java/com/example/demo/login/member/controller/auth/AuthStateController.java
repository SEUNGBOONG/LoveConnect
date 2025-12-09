//package com.example.demo.login.member.controller.auth;
//
//import com.example.demo.common.exception.Setting;
//import com.example.demo.login.email.util.EmailSenderUtil;
//import com.example.demo.login.member.controller.auth.dto.ChangePasswordRequest;
//import com.example.demo.login.member.controller.auth.dto.EmailCheckRequest;
//import com.example.demo.login.member.exception.exceptions.auth.AuthenticateEmailFirstException;
//import com.example.demo.login.member.service.auth.AuthService;
//import jakarta.mail.MessagingException;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.io.UnsupportedEncodingException;
//
//@RestController
//@RequestMapping
//@RequiredArgsConstructor
//@Slf4j
//public class AuthStateController {
//
//    private final EmailSenderUtil emailSenderUtil;
//    private final AuthService authService;
//
//    @PostMapping("/change-password")
//    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest request) {
//        if (!emailSenderUtil.isEmailVerified(request.getEmail())) {
//            throw new AuthenticateEmailFirstException();
//        }
//
//        authService.changePassword(request.getEmail(), request.getNewPassword(), request.getNewPasswordConfirm());
//        emailSenderUtil.clearVerifiedFlag(request.getEmail());
//        return ResponseEntity.ok(Setting.PASSWORD_CHANGE_SUCCESS.toString());
//    }
//}
