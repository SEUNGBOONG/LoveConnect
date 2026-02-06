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

import java.util.Base64;
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

    /**
     * 토스 로그인 메인 플로우
     */
    @Transactional
    public Map<String, Object> executeTossLogin(
            String authorizationCode,
            String referrer
    ) {

        /* ==================================================
         * 0. 복호화 설정 검증 (여기서 터지면 100% 설정 문제)
         * ================================================== */
        validateDecryptConfig();

        /* =======================
         * 1. AccessToken 발급
         * ======================= */
        String tokenUrl =
                baseUrl + "/api-partner/v1/apps-in-toss/user/oauth2/generate-token";

        Map<String, String> body = Map.of(
                "authorizationCode", authorizationCode,
                "referrer", referrer   // 🔥 프론트에서 받은 그대로
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map> tokenResponse =
                tossRestTemplate.postForEntity(
                        tokenUrl,
                        new HttpEntity<>(body, headers),
                        Map.class
                );

        Map tokenBody = tokenResponse.getBody();

        if (tokenBody == null || !"SUCCESS".equals(tokenBody.get("resultType"))) {
            log.error("[TOSS] token issue failed: {}", tokenBody);
            throw new IllegalStateException("토스 AccessToken 발급 실패");
        }

        Map success = (Map) tokenBody.get("success");
        String accessToken = (String) success.get("accessToken");

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

        if (infoBody == null || !"SUCCESS".equals(infoBody.get("resultType"))) {
            log.error("[TOSS] user info failed: {}", infoBody);
            throw new IllegalStateException("토스 사용자 정보 조회 실패");
        }

        Map user = (Map) infoBody.get("success");

        /* =======================
         * 3. 복호화 (실패 시 즉시 중단)
         * ======================= */
        String name = decryptRequired(user, "name");
        String phone = decryptRequired(user, "phone");
        String ci = decryptRequired(user, "ci");

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
                                .memberNickName("토스_" + UUID.randomUUID().toString().substring(0, 6))
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

    /* ==================================================
     * 내부 헬퍼 메서드
     * ================================================== */

    /**
     * 복호화 필수 값 처리 (실패 시 즉시 예외)
     */
    private String decryptRequired(Map user, String field) {
        Object value = user.get(field);
        if (value == null) {
            throw new IllegalStateException("토스 응답에 " + field + " 값이 없습니다.");
        }

        try {
            return TossDecryptor.decrypt(
                    (String) value,
                    decryptKey,
                    decryptAad
            );
        } catch (Exception e) {
            log.error("[TOSS] decrypt failed - field={}", field, e);
            throw new IllegalStateException("토스 사용자 정보 복호화 실패 (" + field + ")");
        }
    }

    /**
     * 복호화 설정 검증
     */
    private void validateDecryptConfig() {
        byte[] keyBytes = Base64.getDecoder().decode(decryptKey);

        if (keyBytes.length != 32) {
            throw new IllegalStateException(
                    "TOSS_DECRYPT_KEY 길이 오류 (expected 32, actual " + keyBytes.length + ")"
            );
        }

        if (decryptAad == null || decryptAad.isBlank()) {
            throw new IllegalStateException("TOSS_DECRYPT_AAD 값이 비어있습니다.");
        }

        log.info("[TOSS] decrypt config OK (keyLength=32, aad='{}')", decryptAad);
    }

    /* =======================
     * 부가 API
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
