package com.example.demo.login.toss.application;

import com.example.demo.common.util.AESUtil;
import com.example.demo.config.toss.TossDecryptor;
import com.example.demo.login.member.domain.member.Member;
import com.example.demo.login.member.infrastructure.auth.JwtTokenProvider;
import com.example.demo.login.member.infrastructure.member.MemberJpaRepository;
import com.example.demo.login.toss.dto.request.TossAdditionalInfoRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

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
    public Map<String, Object> executeTossLogin(
            String authorizationCode,
            String referrer
    ) {

        if (authorizationCode == null || authorizationCode.isBlank()) {
            throw new IllegalArgumentException("authorizationCode가 비어있습니다.");
        }

        if (referrer == null || referrer.isBlank()) {
            throw new IllegalArgumentException("referrer가 비어있습니다.");
        }

        /* =======================
         * 1. AccessToken 발급
         * ======================= */
        String tokenUrl =
                baseUrl + "/api-partner/v1/apps-in-toss/user/oauth2/generate-token";

        Map<String, String> body = Map.of(
                "authorizationCode", authorizationCode,
                "referrer", referrer
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> tokenResponse;
        try {
            tokenResponse = tossRestTemplate.postForEntity(tokenUrl, entity, Map.class);
        } catch (Exception e) {
            log.error("[TOSS] token API call failed", e);
            throw new IllegalStateException("토스 토큰 API 호출 실패");
        }

        Map tokenBody = tokenResponse.getBody();
        log.info("[TOSS] token response = {}", tokenBody);

        if (tokenBody == null || !(tokenBody.get("success") instanceof Map)) {
            Object error = tokenBody != null ? tokenBody.get("error") : null;
            throw new IllegalStateException("토스 토큰 발급 실패: " + error);
        }

        Map success = (Map) tokenBody.get("success");
        String accessToken = (String) success.get("accessToken");

        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalStateException("accessToken이 비어있습니다.");
        }

        /* =======================
         * 2. 사용자 정보 조회
         * ======================= */
        String infoUrl =
                baseUrl + "/api-partner/v1/apps-in-toss/user/oauth2/login-me";

        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(accessToken);

        ResponseEntity<Map> infoResponse =
                tossRestTemplate.exchange(
                        infoUrl,
                        HttpMethod.GET,
                        new HttpEntity<>(authHeaders),
                        Map.class
                );

        Map infoBody = infoResponse.getBody();
        log.info("[TOSS] user info response = {}", infoBody);

        if (infoBody == null || !(infoBody.get("success") instanceof Map)) {
            throw new IllegalStateException("토스 사용자 정보 조회 실패");
        }

        Map user = (Map) infoBody.get("success");

        /* =======================
         * 3. 복호화
         * ======================= */
        String name = decryptOrNull((String) user.get("name"));
        String phone = decryptOrNull((String) user.get("phone"));
        String ci = decryptOrNull((String) user.get("ci"));

        if (phone == null || ci == null) {
            throw new IllegalStateException("필수 사용자 정보 복호화 실패");
        }

        String cleanPhone = phone.replaceAll("[^0-9]", "");
        String encryptedPhone = AESUtil.encrypt(cleanPhone);

        /* =======================
         * 4. 회원 처리
         * ======================= */
        Optional<Member> optional =
                memberRepository.findByPhoneNumber(encryptedPhone);

        boolean isNewMember = optional.isEmpty();

        Member member = optional.orElseGet(() ->
                memberRepository.save(
                        Member.builder()
                                .memberName(name)
                                .phoneNumber(encryptedPhone)
                                .memberEmail(cleanPhone + "@toss.user")
                                .memberNickName(
                                        "토스_" + UUID.randomUUID().toString().substring(0, 6)
                                )
                                .memberPassword(UUID.randomUUID().toString())
                                .build()
                )
        );

        member.setTossCi(ci);

        /* =======================
         * 5. JWT 발급
         * ======================= */
        String jwt = jwtTokenProvider.createToken(member.getId());

        return Map.of(
                "token", jwt,
                "isNewMember", isNewMember,
                "memberId", member.getId(),
                "nickname", member.getMemberNickName()
        );
    }

    private String decryptOrNull(String encrypted) {
        if (encrypted == null) return null;
        try {
            return TossDecryptor.decrypt(encrypted, decryptKey, decryptAad);
        } catch (Exception e) {
            log.error("[TOSS] decrypt failed", e);
            return null;
        }
    }

    /* =======================
     * 부가 기능
     * ======================= */
    @Transactional
    public void updateMemberProfile(Long memberId, TossAdditionalInfoRequest request) {
        Member member = memberRepository.findById(memberId).orElseThrow();

        member.updateProfile(
                request.nickname(),
                request.instagramId(),
                request.tiktokId(),
                request.mbti(),
                request.emailAgree()
        );
    }

    @Transactional
    public void disconnectByCi(String ci) {
        Member member = memberRepository.findByTossCi(ci).orElseThrow();
        member.disconnectToss();
    }
}
