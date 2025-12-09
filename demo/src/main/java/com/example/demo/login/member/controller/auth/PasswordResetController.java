//package com.example.demo.login.member.controller.auth;
//
//import com.example.demo.common.exception.ApiResponse;
//import com.example.demo.common.util.AESUtil;
//import com.example.demo.login.member.controller.auth.dto.ResetPasswordChangeRequest;
//import com.example.demo.login.member.controller.auth.dto.ResetPasswordRequest;
//import com.example.demo.login.member.service.auth.AuthService;
//import com.example.demo.login.member.service.auth.PhoneAuthService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequiredArgsConstructor
//public class PasswordResetController {
//
//    private final PhoneAuthService phoneAuthService;
//    private final AuthService authService;
//
//    // 1단계: 인증번호 전송
//    @PostMapping("/reset-password/send-code")
//    public ResponseEntity<ApiResponse<String>> sendResetCode(@RequestBody ResetPasswordRequest request) {
//        phoneAuthService.sendVerificationCode(request.getPhoneNumber());
//        return ResponseEntity.ok(ApiResponse.success("인증번호가 전송되었습니다."));
//    }
//
//    // 2단계: 인증 후 비밀번호 변경
//    @PostMapping("/reset-password")
//    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody ResetPasswordChangeRequest request) {
//        String phoneNumber = request.getPhoneNumber();
//
//        if (!phoneAuthService.isVerified(phoneNumber)) {
//            return ResponseEntity.badRequest().body(
//                    ApiResponse.fail("PHONE_NOT_VERIFIED", "인증되지 않은 전화번호입니다.")
//            );
//        }
//
//        authService.changePassword(request.getEmail(), request.getNewPassword(), request.getConfirmPassword());
//        phoneAuthService.clearVerified(phoneNumber);
//
//        return ResponseEntity.ok(ApiResponse.success("비밀번호가 성공적으로 변경되었습니다."));
//    }
//}
