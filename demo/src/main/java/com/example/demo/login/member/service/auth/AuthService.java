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

    // AuthService 내부에 추가

    @Transactional
    public void withdrawMember(Long memberId) {
        Member member = getById(memberId);

        // 1. 일반 인스타 매칭 요청 삭제 (상대방과의 연결 고리 제거)
        matchRequestRepository.findByRequester(member).ifPresent(matchRequestRepository::delete);

        // 2. 틱톡 매칭 요청 삭제
        tiktokMatchRequestRepository.findByRequester(member).ifPresent(tiktokMatchRequestRepository::delete);

        // 3. 회원 탈퇴(Soft Delete 등) 처리
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

        String instagramId = request.instagramId();
        String tiktokId = request.tiktokId();

        String processedInstagramId =
                instagramId == null || instagramId.isBlank()
                        ? null
                        : AESUtil.encrypt(instagramId.trim().toLowerCase());

        String processedTiktokId =
                tiktokId == null || tiktokId.isBlank()
                        ? null
                        : AESUtil.encrypt(tiktokId.trim().toLowerCase());

        member.updateProfile(
                request.nickname(),          // null이면 유지
                processedInstagramId,        // 🔥 null 허용
                processedTiktokId,           // 🔥 null 허용
                request.mbti(),
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
        Member member = memberJpaRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));

        // null이나 공백이 들어오면 데이터를 삭제(null 처리)하도록 변경
        if (rawTiktokId == null || rawTiktokId.trim().isBlank()) {
            member.updateTiktokId(null);
            return;
        }

        // 기존에 등록되어 있어도 덮어쓰기 가능하도록 '이미 등록된 경우 차단' 로직 제거
        member.updateTiktokId(
                AESUtil.encrypt(rawTiktokId.trim().toLowerCase())
        );
    }

    public String generateToken(Long memberId) {
        return jwtTokenProvider.createToken(memberId);
    }
}
