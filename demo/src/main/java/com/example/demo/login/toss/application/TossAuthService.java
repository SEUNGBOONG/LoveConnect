package com.example.demo.login.toss.application;

import com.example.demo.common.util.AESUtil;
import com.example.demo.config.toss.TossDecryptor;
import com.example.demo.login.global.exception.exceptions.CustomErrorCode;
import com.example.demo.login.global.exception.exceptions.CustomException;
import com.example.demo.login.member.domain.member.Member;
import com.example.demo.login.member.infrastructure.auth.JwtTokenProvider;
import com.example.demo.login.member.infrastructure.member.MemberJpaRepository;
import com.example.demo.login.toss.dto.request.TossAdditionalInfoRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TossAuthService {

    private final RestTemplate tossRestTemplate;
    private final MemberJpaRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${toss.api.base-url}")
    private String baseUrl;

    @Value("${toss.decrypt.key}")
    private String decryptKey;

    @Value("${toss.decrypt.aad}")
    private String decryptAad;

    @Transactional
    public Map<String, Object> executeTossLogin(String authorizationCode, String referrer) throws Exception {

        String normalizedReferrer = normalizeReferrer(referrer);

        // 1. 토큰 발급 (referrer는 토스 API 필수값)
        String tokenUrl = baseUrl + "/api-partner/v1/apps-in-toss/user/oauth2/generate-token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("authorizationCode", authorizationCode);
        body.put("referrer", normalizedReferrer);

        ResponseEntity<Map> tokenResponse;
        try {
            tokenResponse =
                    tossRestTemplate.postForEntity(tokenUrl, new HttpEntity<>(body, headers), Map.class);
        } catch (HttpStatusCodeException e) {
            log.error("[TOSS] token issue failed. status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new CustomException(CustomErrorCode.TOSS_TOKEN_FAILED);
        }

        Map tokenBody = tokenResponse.getBody();

        if (tokenBody == null || !(tokenBody.get("success") instanceof Map success)) {
            log.error("[TOSS] token issue failed: {}", tokenBody);
            throw new CustomException(CustomErrorCode.TOSS_TOKEN_FAILED);
        }

        String accessToken = (String) success.get("accessToken");
        if (!StringUtils.hasText(accessToken)) {
            log.error("[TOSS] accessToken missing: {}", tokenBody);
            throw new CustomException(CustomErrorCode.TOSS_TOKEN_FAILED);
        }

        // 2. 사용자 정보 조회
        String infoUrl = baseUrl + "/api-partner/v1/apps-in-toss/user/oauth2/login-me";

        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(accessToken);

        ResponseEntity<Map> infoResponse;
        try {
            infoResponse =
                    tossRestTemplate.exchange(infoUrl, HttpMethod.GET, new HttpEntity<>(authHeaders), Map.class);
        } catch (HttpStatusCodeException e) {
            log.error("[TOSS] user info failed. status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new CustomException(CustomErrorCode.TOSS_USER_INFO_FAILED);
        }

        Map infoBody = infoResponse.getBody();

        if (infoBody == null || !(infoBody.get("success") instanceof Map user)) {
            log.error("[TOSS] user info failed: {}", infoBody);
            throw new CustomException(CustomErrorCode.TOSS_USER_INFO_FAILED);
        }

        Object userKeyObj = user.get("userKey");
        if (!(userKeyObj instanceof Number)) {
            log.error("[TOSS] userKey missing: {}", user);
            throw new CustomException(CustomErrorCode.TOSS_USER_INFO_FAILED);
        }
        Long userKey = ((Number) userKeyObj).longValue();

        String name = TossDecryptor.decryptIfPresent((String) user.get("name"), decryptKey, decryptAad);
        String decryptedPhone = TossDecryptor.decryptIfPresent((String) user.get("phone"), decryptKey, decryptAad);
        String ci = TossDecryptor.decryptIfPresent((String) user.get("ci"), decryptKey, decryptAad);
        String birthday = TossDecryptor.decryptIfPresent((String) user.get("birthday"), decryptKey, decryptAad);
        String gender = TossDecryptor.decryptIfPresent((String) user.get("gender"), decryptKey, decryptAad);

        if (!StringUtils.hasText(decryptedPhone)) {
            log.error("[TOSS] phone missing after decrypt. userKey={}", userKey);
            throw new CustomException(CustomErrorCode.TOSS_USER_INFO_FAILED);
        }

        String cleanPhone = normalizePhone(decryptedPhone);
        String encryptedPhone = AESUtil.encrypt(cleanPhone);
        String safeName = StringUtils.hasText(name) ? name : "토스사용자";
        String safeBirthday = StringUtils.hasText(birthday) ? birthday : "00000000";
        String safeGender = StringUtils.hasText(gender) ? gender : "UNKNOWN";

        // 3. 회원 조회 또는 생성 후 토스 계정 연동
        Member member = resolveMember(ci, userKey, encryptedPhone)
                .orElseGet(() -> createNewTossMember(
                        safeName, encryptedPhone, cleanPhone, userKey, safeBirthday, safeGender
                ));

        member = linkTossIdentity(member, ci, userKey);

        boolean isNewMember = isAdditionalInfoMissing(member);
        String jwt = jwtTokenProvider.createToken(member.getId());

        return Map.of(
                "token", jwt,
                "isNewMember", isNewMember,
                "memberId", member.getId(),
                "nickname", member.getMemberNickName()
        );
    }

    private Member createNewTossMember(
            String name,
            String encryptedPhone,
            String cleanPhone,
            Long userKey,
            String birthday,
            String gender
    ) {
        try {
            return memberRepository.save(
                    Member.builder()
                            .memberName(name)
                            .phoneNumber(encryptedPhone)
                            .birthDate(birthday)
                            .gender(gender)
                            .instagramId("")
                            .tiktokId("")
                            .mbti("")
                            .emailAgree(true)
                            .privacyAgree(true)
                            .useAgree(true)
                            .memberEmail(generateUniqueTossEmail(cleanPhone, userKey))
                            .memberNickName(generateUniqueNickname())
                            .memberPassword(UUID.randomUUID().toString())
                            .build()
            );
        } catch (DataIntegrityViolationException e) {
            log.warn("[TOSS] concurrent member creation, retrying lookup. phone={}", encryptedPhone, e);
            return memberRepository.findByPhoneNumber(encryptedPhone)
                    .filter(m -> !m.isDeleted())
                    .orElseThrow(() -> new CustomException(CustomErrorCode.TOSS_MEMBER_CONFLICT));
        }
    }

    private Member linkTossIdentity(Member member, String ci, Long userKey) {
        member.setTossCi(ci);
        member.setUserKey(userKey);

        try {
            return memberRepository.saveAndFlush(member);
        } catch (DataIntegrityViolationException e) {
            log.warn("[TOSS] identity conflict on memberId={}, retrying with existing identity", member.getId(), e);

            Optional<Member> byCi = StringUtils.hasText(ci)
                    ? memberRepository.findByTossCi(ci).filter(m -> !m.isDeleted())
                    : Optional.empty();
            if (byCi.isPresent()) {
                Member existing = byCi.get();
                existing.setUserKey(userKey);
                return memberRepository.saveAndFlush(existing);
            }

            Optional<Member> byUserKey = userKey != null
                    ? memberRepository.findByUserKey(userKey).filter(m -> !m.isDeleted())
                    : Optional.empty();
            if (byUserKey.isPresent()) {
                Member existing = byUserKey.get();
                existing.setTossCi(ci);
                return memberRepository.saveAndFlush(existing);
            }

            log.error("[TOSS] member upsert failed. memberId={}, ci={}, userKey={}", member.getId(), ci, userKey, e);
            throw new CustomException(CustomErrorCode.TOSS_MEMBER_CONFLICT);
        }
    }

    private String normalizeReferrer(String referrer) {
        if (!StringUtils.hasText(referrer)) {
            log.warn("[TOSS] referrer missing, defaulting to DEFAULT");
            return "DEFAULT";
        }
        String trimmed = referrer.trim();
        if ("SANDBOX".equalsIgnoreCase(trimmed)) {
            return "SANDBOX";
        }
        if ("DEFAULT".equalsIgnoreCase(trimmed)) {
            return "DEFAULT";
        }
        return trimmed;
    }

    private String normalizePhone(String phone) {
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.startsWith("82") && digits.length() >= 10) {
            digits = "0" + digits.substring(2);
        }
        return digits;
    }

    private String generateUniqueTossEmail(String cleanPhone, Long userKey) {
        String baseEmail = cleanPhone + "@toss.user";
        if (!memberRepository.existsByMemberEmail(baseEmail)) {
            return baseEmail;
        }
        return userKey + "_" + cleanPhone + "@toss.user";
    }

    private String generateUniqueNickname() {
        String nickname;
        do {
            nickname = "토스_" + UUID.randomUUID().toString().substring(0, 6);
        } while (memberRepository.existsByMemberNickName(nickname));
        return nickname;
    }

    private boolean isAdditionalInfoMissing(Member member) {
        return member.getInstagramId() == null
                || member.getInstagramId().isBlank()
                || member.getMbti() == null
                || member.getMbti().isBlank();
    }

    private Optional<Member> resolveMember(String ci, Long userKey, String encryptedPhone) {
        if (StringUtils.hasText(ci)) {
            Optional<Member> byCi = memberRepository.findByTossCi(ci).filter(m -> !m.isDeleted());
            if (byCi.isPresent()) {
                return byCi;
            }
        }

        if (userKey != null) {
            Optional<Member> byUserKey = memberRepository.findByUserKey(userKey).filter(m -> !m.isDeleted());
            if (byUserKey.isPresent()) {
                return byUserKey;
            }
        }

        return memberRepository.findByPhoneNumber(encryptedPhone).filter(m -> !m.isDeleted());
    }

    @Transactional
    public void updateMemberProfile(Long memberId, TossAdditionalInfoRequest request) {
        Member member = memberRepository.findById(memberId).orElseThrow();
        member.updateTossProfile(
                request.nickname(),
                request.instagramId(),
                request.tiktokId(),
                request.mbti()
        );
    }

    @Transactional
    public void disconnectByUserKey(Long userKey) {
        Member member = memberRepository.findByUserKey(userKey)
                .orElseThrow(() -> new IllegalArgumentException("해당 userKey로 등록된 사용자가 없습니다."));
        member.disconnectToss();
    }
}
