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
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class TossAuthService {

    private final RestTemplate tossRestTemplate;
    private final MemberJpaRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    // 🔒 authorizationCode 1회 보장
    private static final Set<String> USED_CODES = ConcurrentHashMap.newKeySet();

    @Value("${toss.api.base-url}")
    private String baseUrl;

    @Value("${toss.decrypt.key}")
    private String decryptKey;

    @Value("${toss.decrypt.aad}")
    private String decryptAad;

    @Transactional
    public Map<String, Object> executeTossLogin(String authCode) throws Exception {

        if (authCode == null || authCode.isBlank()) {
            throw new IllegalArgumentException("authorizationCode 누락");
        }

        // 🔥 중복 코드 차단
        if (!USED_CODES.add(authCode)) {
            log.warn("[TOSS] duplicate authorizationCode blocked: {}", authCode);
            throw new IllegalArgumentException("이미 사용된 인증 코드입니다. 다시 로그인하세요.");
        }

        log.info("[TOSS] generate-token start. authCode={}", authCode);

        /* 1. 토큰 발급 */
        String tokenUrl = baseUrl + "/api-partner/v1/apps-in-toss/user/oauth2/generate-token";

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(
                Map.of("authorizationCode", authCode, "referrer", "DEFAULT"),
                new HttpHeaders() {{
                    setContentType(MediaType.APPLICATION_JSON);
                }}
        );

        ResponseEntity<Map> tokenResponse =
                tossRestTemplate.postForEntity(tokenUrl, entity, Map.class);

        Map tokenBody = tokenResponse.getBody();

        if (tokenBody == null || !(tokenBody.get("success") instanceof Map)) {
            log.error("[TOSS] token issue failed: {}", tokenBody);
            throw new IllegalArgumentException("토스 인증이 만료되었습니다. 다시 로그인하세요.");
        }

        String accessToken =
                (String) ((Map<?, ?>) tokenBody.get("success")).get("accessToken");

        /* 2. 사용자 정보 */
        ResponseEntity<Map> infoResponse =
                tossRestTemplate.exchange(
                        baseUrl + "/api-partner/v1/apps-in-toss/user/oauth2/login-me",
                        HttpMethod.GET,
                        new HttpEntity<>(new HttpHeaders() {{
                            setBearerAuth(accessToken);
                        }}),
                        Map.class
                );

        Map infoBody = infoResponse.getBody();
        if (infoBody == null || !(infoBody.get("success") instanceof Map)) {
            throw new IllegalStateException("토스 사용자 정보 조회 실패");
        }

        Map user = (Map) infoBody.get("success");

        /* 3. 복호화 */
        String name = TossDecryptor.decrypt((String) user.get("name"), decryptKey, decryptAad);
        String phone = TossDecryptor.decrypt((String) user.get("phone"), decryptKey, decryptAad);
        String ci = TossDecryptor.decrypt((String) user.get("ci"), decryptKey, decryptAad);

        String encryptedPhone = AESUtil.encrypt(phone.replaceAll("[^0-9]", ""));

        /* 4. 회원 처리 */
        Optional<Member> optional = memberRepository.findByPhoneNumber(encryptedPhone);
        boolean isNew = optional.isEmpty();

        Member member = optional.orElseGet(() ->
                memberRepository.save(
                        Member.builder()
                                .memberName(name)
                                .phoneNumber(encryptedPhone)
                                .memberEmail(phone + "@toss.user")
                                .memberNickName("토스_" + UUID.randomUUID().toString().substring(0, 6))
                                .memberPassword(UUID.randomUUID().toString())
                                .build()
                )
        );

        member.setTossCi(ci);

        /* 5. JWT */
        return Map.of(
                "token", jwtTokenProvider.createToken(member.getId()),
                "isNewMember", isNew,
                "memberId", member.getId(),
                "nickname", member.getMemberNickName()
        );
    }

    @Transactional
    public void updateMemberProfile(Long memberId, TossAdditionalInfoRequest request) {
        memberRepository.findById(memberId)
                .orElseThrow()
                .updateProfile(
                        request.nickname(),
                        request.instagramId(),
                        request.tiktokId(),
                        request.mbti(),
                        request.emailAgree()
                );
    }

    @Transactional
    public void disconnectByCi(String ci) {
        memberRepository.findByTossCi(ci)
                .orElseThrow()
                .disconnectToss();
    }
}
