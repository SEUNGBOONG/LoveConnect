package com.example.demo.login.member.service.auth;

import com.example.demo.common.util.AESUtil;
import com.example.demo.login.global.exception.exceptions.CustomErrorCode;
import com.example.demo.login.global.exception.exceptions.CustomException;
import com.example.demo.login.member.controller.auth.dto.LoginRequest;
import com.example.demo.login.member.controller.auth.dto.MemberUpdateRequest;
import com.example.demo.login.member.controller.auth.dto.NormalSignUpRequest;
import com.example.demo.login.member.controller.auth.dto.PasswordResetRequest;
import com.example.demo.login.member.domain.auth.EmailValidator;
import com.example.demo.login.member.domain.auth.SignUpValidator;
import com.example.demo.login.member.domain.member.Member;
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
    private final PhoneAuthService phoneAuthService;

    public Member normalSignUp(NormalSignUpRequest request) {

        if (!phoneAuthService.isVerified(request.phoneNumber())) {
            throw new CustomException(CustomErrorCode.PHONE_AUTH_REQUIRED);
        }

        emailValidator.validateEmailFormat(request.email());
        signUpValidator.normalValidateSignupRequestFormat(request);

        authValidator.checkDuplicateMemberNickName(request.nickname());
        authValidator.checkDuplicateMemberEmail(request.email());
        authValidator.checkDuplicatePhoneNumber(request.phoneNumber());

        String encodedPassword = passwordEncoder.encode(request.password());
        Member member = AuthMapper.toNormalMember(request, encodedPassword);

        phoneAuthService.clearVerified(request.phoneNumber());

        return memberJpaRepository.save(member);
    }

    @Transactional
    public void withdrawMember(Long memberId) {
        Member member = getById(memberId);
        member.withdraw();
    }

    @Transactional(readOnly = true)
    public Member loginAndReturnMember(LoginRequest request) {
        Member member = authValidator.findMemberByEmail(request.memberEmail());

        if (member.isDeleted()) {
            throw new CustomException(CustomErrorCode.MEMBER_WITHDRAWN);
        }

        boolean isMatch =
                passwordEncoder.matches(
                        request.memberPassword(),
                        member.getMemberPassword()
                );

        AuthValidator.validatePasswordMatch(isMatch);

        return member;
    }

    public Member getById(Long id) {
        return memberJpaRepository.findById(id)
                .orElseThrow(() ->
                        new CustomException(CustomErrorCode.MEMBER_NOT_FOUND)
                );
    }

    /* ===============================
       🔥 프로필 수정 (인스타 + 틱톡)
       =============================== */

    @Transactional
    public void updateProfile(Long memberId, MemberUpdateRequest request) {
        Member member = getById(memberId);

        member.updateProfile(
                request.nickname(),
                request.instagramId(),
                request.tiktokId(),   // 🔥 추가
                request.mbti(),
                request.emailAgree()
        );
    }

    @Transactional
    public void resetPassword(PasswordResetRequest request) {

        if (!phoneAuthService.isVerified(request.phoneNumber())) {
            throw new CustomException(CustomErrorCode.PHONE_AUTH_REQUIRED);
        }

        String encryptedPhone =
                AESUtil.encrypt(request.phoneNumber());

        Member member =
                memberJpaRepository.findByPhoneNumber(encryptedPhone)
                        .orElseThrow(() ->
                                new CustomException(CustomErrorCode.MEMBER_NOT_FOUND)
                        );

        String encodedPassword =
                passwordEncoder.encode(request.newPassword());

        member.changePassword(encodedPassword);

        phoneAuthService.clearVerified(request.phoneNumber());
    }

    public String generateToken(Long memberId) {
        return jwtTokenProvider.createToken(memberId);
    }
}
