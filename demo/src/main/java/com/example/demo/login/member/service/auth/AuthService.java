package com.example.demo.login.member.service.auth;

import com.example.demo.common.util.AESUtil;
import com.example.demo.login.member.controller.auth.dto.LoginRequest;
import com.example.demo.login.member.controller.auth.dto.MemberUpdateRequest;
import com.example.demo.login.member.controller.auth.dto.NormalSignUpRequest;
import com.example.demo.login.member.controller.auth.dto.PasswordResetRequest;
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
        authValidator.checkDuplicatePhoneNumber(request.phoneNumber());

        String encodedPassword = passwordEncoder.encode(request.password());
        Member member = AuthMapper.toNormalMember(request, encodedPassword);

        // 🔥 3) 회원가입 완료 후 인증상태 제거 (선택적)
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

        // 🔒 소프트 딜리트된 회원 차단
        if (member.isDeleted()) {
            throw new CustomException(CustomErrorCode.MEMBER_WITHDRAWN);
        }
        boolean isMatch = passwordEncoder.matches(request.memberPassword(), member.getMemberPassword());
        AuthValidator.validatePasswordMatch(isMatch);

        return member;
    }

    public Member getById(Long id) {
        return memberJpaRepository.findById(id)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));
    }

    @Transactional
    public void updateProfile(Long memberId, MemberUpdateRequest request) {
        Member member = getById(memberId);

        member.updateProfile(
                request.nickname(),
                request.instagramId(),
                request.titokId(),  // ✅ 3번째: tiktok
                request.mbti(),     // ✅ 4번째: mbti
                request.emailAgree()
        );
    }

    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        // 1️⃣ 인증 여부 체크 (Redis)
        if (!phoneAuthService.isVerified(request.phoneNumber())) {
            throw new CustomException(CustomErrorCode.PHONE_AUTH_REQUIRED);
        }

        // 2️⃣ 암호화된 전화번호로 사용자 조회
        String encryptedPhone = AESUtil.encrypt(request.phoneNumber());

        Member member = memberJpaRepository.findByPhoneNumber(encryptedPhone)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));

        // 3️⃣ 비밀번호 암호화 후 저장
        String encodedPassword = passwordEncoder.encode(request.newPassword());
        member.changePassword(encodedPassword);

        // 4️⃣ 인증 정보 제거 (1회성)
        phoneAuthService.clearVerified(request.phoneNumber());
    }

    @Transactional
    public void registerTiktokId(Long memberId, String rawTiktokId) {

        if (rawTiktokId == null || rawTiktokId.isBlank()) {
            throw new CustomException(CustomErrorCode.INVALID_TIKTOK_ID);
        }

        Member member = memberJpaRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MATCH_MEMBER_NOT_FOUND));

        // 이미 등록된 경우 차단
        if (member.getTiktokId() != null) {
            throw new CustomException(CustomErrorCode.ALREADY_REGISTERED);
        }

        member.updateTiktokId(
                AESUtil.encrypt(rawTiktokId.trim().toLowerCase())
        );
    }

    public String generateToken(Long memberId) {
        return jwtTokenProvider.createToken(memberId);
    }
}
