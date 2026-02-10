package com.example.demo.login.member.service.auth;

import com.example.demo.common.util.AESUtil;
import com.example.demo.login.global.exception.exceptions.CustomErrorCode;
import com.example.demo.login.global.exception.exceptions.CustomException;
import com.example.demo.login.member.controller.auth.dto.LoginRequest;
import com.example.demo.login.member.controller.auth.dto.MemberUpdateRequest;
import com.example.demo.login.member.domain.auth.EmailValidator;
import com.example.demo.login.member.domain.auth.SignUpValidator;
import com.example.demo.login.member.domain.member.Member;
import com.example.demo.login.member.infrastructure.auth.JwtTokenProvider;
import com.example.demo.login.member.infrastructure.member.MemberJpaRepository;
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

    /* ===================== 조회 ===================== */

    public Member getById(Long id) {
        Member member = memberJpaRepository.findById(id)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));

        // 🔒 탈퇴 회원 차단 (조회/수정/로그인용)
        if (member.isDeleted()) {
            throw new CustomException(CustomErrorCode.MEMBER_WITHDRAWN);
        }
        return member;
    }

    /* ===================== 탈퇴 ===================== */

    @Transactional
    public void withdrawMember(Long memberId) {
        Member member = memberJpaRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));

        // ✅ 이미 탈퇴된 경우 → 그냥 종료 (idempotent)
        if (member.isDeleted()) {
            return;
        }

        matchRequestRepository.findByRequester(member)
                .ifPresent(matchRequestRepository::delete);

        tiktokMatchRequestRepository.findByRequester(member)
                .ifPresent(tiktokMatchRequestRepository::delete);

        member.withdraw();
    }

    /* ===================== 로그인 ===================== */

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

    /* ===================== 프로필 ===================== */

    @Transactional
    public void updateProfile(Long memberId, MemberUpdateRequest request) {
        Member member = getById(memberId);

        String instagramId = request.instagramId();
        String tiktokId = request.tiktokId();

        member.updateProfile(
                request.nickname(),
                instagramId == null || instagramId.isBlank() ? null : AESUtil.encrypt(instagramId),
                tiktokId == null || tiktokId.isBlank() ? null : AESUtil.encrypt(tiktokId),
                request.mbti(),
                request.emailAgree()
        );
    }

    public String generateToken(Long memberId) {
        return jwtTokenProvider.createToken(memberId);
    }
}
