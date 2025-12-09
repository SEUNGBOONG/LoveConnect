package com.example.demo.login.member.service.auth;

import com.example.demo.login.member.controller.auth.dto.LoginRequest;
import com.example.demo.login.member.controller.auth.dto.NormalSignUpRequest;
import com.example.demo.login.member.domain.auth.EmailValidator;
import com.example.demo.login.member.domain.auth.SignUpValidator;
import com.example.demo.login.member.infrastructure.auth.JwtTokenProvider;
import com.example.demo.login.member.infrastructure.member.MemberJpaRepository;
import com.example.demo.login.member.domain.member.Member;
import com.example.demo.login.member.mapper.auth.AuthMapper;
import com.example.demo.login.util.AuthValidator;
import com.example.demo.login.global.exception.exceptions.CustomErrorCode;
import com.example.demo.login.global.exception.exceptions.CustomException;
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
    private final PhoneAuthService phoneAuthService;

    public Member normalSignUp(NormalSignUpRequest request) {

        // 🔥 1) 전화번호 인증 여부 체크
        if (!phoneAuthService.isVerified(request.phoneNumber())) {
            throw new CustomException(CustomErrorCode.PHONE_AUTH_REQUIRED);
        }

        // 🔍 2) 나머지 검증
        emailValidator.validateEmailFormat(request.email());
        signUpValidator.normalValidateSignupRequestFormat(request);

        authValidator.checkDuplicateMemberNickName(request.nickname());
        authValidator.checkDuplicateMemberEmail(request.email());

        String encodedPassword = passwordEncoder.encode(request.password());
        Member member = AuthMapper.toNormalMember(request, encodedPassword);

        // 🔥 3) 회원가입 완료 후 인증상태 제거 (선택적)
        phoneAuthService.clearVerified(request.phoneNumber());

        return memberJpaRepository.save(member);
    }

    @Transactional(readOnly = true)
    public Member loginAndReturnMember(LoginRequest request) {
        Member member = authValidator.findMemberByEmail(request.memberEmail());

        boolean isMatch = passwordEncoder.matches(request.memberPassword(), member.getMemberPassword());
        AuthValidator.validatePasswordMatch(isMatch);

        return member;
    }

    public String generateToken(Long memberId) {
        return jwtTokenProvider.createToken(memberId);
    }
}
