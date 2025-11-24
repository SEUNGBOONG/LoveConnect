package com.example.demo.login.member.service.auth;

import com.example.demo.login.member.controller.auth.dto.LoginRequest;
import com.example.demo.login.member.controller.auth.dto.NormalSignUpRequest;
import com.example.demo.login.member.domain.auth.EmailValidator;
import com.example.demo.login.member.domain.auth.SignUpValidator;
import com.example.demo.login.member.domain.member.Member;
import com.example.demo.login.member.exception.exceptions.MemberErrorCode;
import com.example.demo.login.member.exception.exceptions.MemberException;
import com.example.demo.login.member.exception.exceptions.auth.NotSamePasswordException;
import com.example.demo.login.member.infrastructure.auth.JwtTokenProvider;
import com.example.demo.login.member.infrastructure.member.MemberJpaRepository;
import com.example.demo.login.member.mapper.auth.AuthMapper;
import com.example.demo.login.util.AuthValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberJpaRepository memberJpaRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final SignUpValidator signUpValidator;
    private final EmailValidator emailValidator;
    private final AuthValidator authValidator;
    private final PhoneAuthService phoneAuthService;   // ✅ 변경된 부분

    public Member normalSignUp(NormalSignUpRequest signUpRequest) {

        // ✅ 전화번호 인증 여부 체크 (변경된 로직)
        if (!phoneAuthService.isVerified(signUpRequest.phoneNumber())) {
            throw new MemberException(MemberErrorCode.PHONE_AUTH_REQUIRED);
        }

        // ✅ 요청 형식 검증
        signUpValidator.normalValidateSignupRequestFormat(signUpRequest);
        emailValidator.validateEmailFormat(signUpRequest.email());

        // ✅ 중복 체크
        authValidator.checkDuplicateMemberNickName(signUpRequest.name());
        authValidator.checkDuplicateMemberEmail(signUpRequest.email());

        // ✅ 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(signUpRequest.password());
        Member member = AuthMapper.toNormalMember(signUpRequest, encodedPassword);

        // ✅ DB 저장
        Member savedMember = memberJpaRepository.save(member);

        // ✅ 회원가입 완료 후 인증 플래그 삭제 (선택)
        phoneAuthService.clearVerified(signUpRequest.phoneNumber());

        return savedMember;
    }

    @Transactional(readOnly = true)
    public Member loginAndReturnMember(LoginRequest loginRequest) {
        signUpValidator.validateLoginRequestFormat(loginRequest);
        Member member = authValidator.findMemberByEmail(loginRequest.memberEmail());

        AuthValidator.validatePasswordEncoderException(
                passwordEncoder.matches(loginRequest.memberPassword(), member.getMemberPassword())
        );

        return member;
    }

    public String generateToken(Long memberId) {
        return jwtTokenProvider.createToken(memberId);
    }

    @Transactional
    public void changePassword(String email, String newPassword, String newPasswordConfirm) {
        Member member = authValidator.findMemberByEmail(email);
        validateNewPassword(newPassword, newPasswordConfirm);
        signUpValidator.checkPasswordLength(newPassword);
        String encodedPassword = passwordEncoder.encode(newPassword);  // 암호화
        member.updatePassword(encodedPassword);
    }

    private static void validatePasswordEncoderException(final boolean passwordEncoder) {
        if (!passwordEncoder) {
            throw new NotSamePasswordException();
        }
    }

    private static void validateNewPassword(final String newPassword, final String newPasswordConfirm) {
        validatePasswordEncoderException(newPassword.equals(newPasswordConfirm));
    }
}
