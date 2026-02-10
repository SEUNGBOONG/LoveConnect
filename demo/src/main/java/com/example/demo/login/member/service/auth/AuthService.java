// 생략된 import 포함 전부 포함
package com.example.demo.login.member.service.auth;

import com.example.demo.common.util.AESUtil;
import com.example.demo.login.global.exception.exceptions.CustomErrorCode;
import com.example.demo.login.global.exception.exceptions.CustomException;
import com.example.demo.login.member.controller.auth.dto.*;
import com.example.demo.login.member.domain.auth.EmailValidator;
import com.example.demo.login.member.domain.auth.SignUpValidator;
import com.example.demo.login.member.domain.member.Member;
import com.example.demo.login.member.infrastructure.auth.JwtTokenProvider;
import com.example.demo.login.member.infrastructure.member.MemberJpaRepository;
import com.example.demo.login.member.mapper.auth.AuthMapper;
import com.example.demo.login.util.AuthValidator;
import com.example.demo.match.domain.MatchRequestRepository;
import com.example.demo.match.domain.TiktokMatchRequestRepository;
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
    private final MatchRequestRepository matchRequestRepository;
    private final TiktokMatchRequestRepository tiktokMatchRequestRepository;

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

    public Member getById(Long id) {
        Member member = memberJpaRepository.findById(id)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));

        if (member.isDeleted()) {
            throw new CustomException(CustomErrorCode.MEMBER_WITHDRAWN);
        }
        return member;
    }

    @Transactional
    public void withdrawMember(Long memberId) {
        Member member = getById(memberId);

        if (member.isDeleted()) return;

        matchRequestRepository.findByRequester(member)
                .ifPresent(matchRequestRepository::delete);

        tiktokMatchRequestRepository.findByRequester(member)
                .ifPresent(tiktokMatchRequestRepository::delete);

        member.withdraw();
    }

    @Transactional(readOnly = true)
    public Member loginAndReturnMember(LoginRequest request) {
        Member member = authValidator.findMemberByEmail(request.memberEmail());

        if (member.isDeleted()) {
            throw new CustomException(CustomErrorCode.MEMBER_WITHDRAWN);
        }

        boolean isMatch = passwordEncoder.matches(
                request.memberPassword(),
                member.getMemberPassword()
        );

        AuthValidator.validatePasswordMatch(isMatch);
        return member;
    }

    @Transactional
    public void updateProfile(Long memberId, MemberUpdateRequest request) {
        Member member = getById(memberId);

        String instagramId = request.instagramId();
        String tiktokId = request.tiktokId();

        member.updateProfile(
                request.nickname(),
                instagramId == null || instagramId.isBlank() ? null : AESUtil.encrypt(instagramId.trim().toLowerCase()),
                tiktokId == null || tiktokId.isBlank() ? null : AESUtil.encrypt(tiktokId.trim().toLowerCase()),
                request.mbti(),
                request.emailAgree()
        );
    }

    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        if (!phoneAuthService.isVerified(request.phoneNumber())) {
            throw new CustomException(CustomErrorCode.PHONE_AUTH_REQUIRED);
        }

        String encryptedPhone = AESUtil.encrypt(request.phoneNumber());

        Member member = memberJpaRepository.findByPhoneNumber(encryptedPhone)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));

        String encodedPassword = passwordEncoder.encode(request.newPassword());
        member.changePassword(encodedPassword);

        phoneAuthService.clearVerified(request.phoneNumber());
    }

    @Transactional
    public void registerTiktokId(Long memberId, String rawTiktokId) {
        Member member = getById(memberId);

        if (rawTiktokId == null || rawTiktokId.trim().isBlank()) {
            member.updateTiktokId(null);
            return;
        }

        member.updateTiktokId(AESUtil.encrypt(rawTiktokId.trim().toLowerCase()));
    }

    public String generateToken(Long memberId) {
        return jwtTokenProvider.createToken(memberId);
    }
}
