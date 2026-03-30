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

        // ✅ 1. 토큰 발급
        String tokenUrl = baseUrl + "/api-partner/v1/apps-in-toss/user/oauth2/generate-token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("authorizationCode", authorizationCode);
        if (StringUtils.hasText(referrer)) {
            body.put("referrer", referrer);
        }

        ResponseEntity<Map> tokenResponse;
        try {
            tokenResponse =
                    tossRestTemplate.postForEntity(tokenUrl, new HttpEntity<>(body, headers), Map.class);
        } catch (HttpStatusCodeException e) {
            log.error("[TOSS] token issue failed. status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new IllegalStateException("토스 토큰 발급 실패");
        }

        Map tokenBody = tokenResponse.getBody();

        if (tokenBody == null || !(tokenBody.get("success") instanceof Map success)) {
            log.error("[TOSS] token issue failed: {}", tokenBody);
            throw new IllegalStateException("토스 토큰 발급 실패");
        }

        String accessToken = (String) success.get("accessToken");

        // ✅ 2. 사용자 정보 조회
        String infoUrl = baseUrl + "/api-partner/v1/apps-in-toss/user/oauth2/login-me";

        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(accessToken);

        ResponseEntity<Map> infoResponse;
        try {
            infoResponse =
                    tossRestTemplate.exchange(infoUrl, HttpMethod.GET, new HttpEntity<>(authHeaders), Map.class);
        } catch (HttpStatusCodeException e) {
            log.error("[TOSS] user info failed. status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new IllegalStateException("토스 사용자 정보 조회 실패");
        }

        Map infoBody = infoResponse.getBody();

        if (infoBody == null || !(infoBody.get("success") instanceof Map user)) {
            log.error("[TOSS] user info failed: {}", infoBody);
            throw new IllegalStateException("토스 사용자 정보 조회 실패");
        }

        // ✅ 복호화
        String name = (String) user.get("name");

        String decryptedPhone =
                TossDecryptor.decrypt((String) user.get("phone"), decryptKey, decryptAad);

        String ci =
                TossDecryptor.decrypt((String) user.get("ci"), decryptKey, decryptAad);

        Long userKey = ((Number) user.get("userKey")).longValue();

        String birthday =
                TossDecryptor.decrypt((String) user.get("birthday"), decryptKey, decryptAad);

        String gender =
                TossDecryptor.decrypt((String) user.get("gender"), decryptKey, decryptAad);

        String cleanPhone = decryptedPhone.replaceAll("[^0-9]", "");
        String encryptedPhone = AESUtil.encrypt(cleanPhone);

        // ✅ 3. 회원 조회 또는 생성
        Optional<Member> optional = memberRepository.findByPhoneNumber(encryptedPhone);

        Member member = optional.orElseGet(() -> memberRepository.save(
                Member.builder()
                        .memberName(name)
                        .phoneNumber(encryptedPhone)
                        .birthDate(birthday)
                        .gender(gender)

                        // 🔥 NOT NULL 방어
                        .instagramId("")
                        .tiktokId("")
                        .mbti("")

                        .emailAgree(true)
                        .privacyAgree(true)
                        .useAgree(true)

                        .memberEmail(cleanPhone + "@toss.user")
                        .memberNickName("토스_" + UUID.randomUUID().toString().substring(0, 6))
                        .memberPassword(UUID.randomUUID().toString())
                        .build()
        ));

        member.setTossCi(ci);
        member.setUserKey(userKey);

        // 🔥🔥🔥 핵심 로직 (여기가 문제였음)
        boolean isNewMember = isAdditionalInfoMissing(member);

        String jwt = jwtTokenProvider.createToken(member.getId());

        return Map.of(
                "token", jwt,
                "isNewMember", isNewMember,
                "memberId", member.getId(),
                "nickname", member.getMemberNickName()
        );
    }

    /**
     ✅ 추가정보 입력 여부 판단
     */
    private boolean isAdditionalInfoMissing(Member member) {

        return member.getInstagramId() == null
                || member.getInstagramId().isBlank()
                || member.getMbti() == null
                || member.getMbti().isBlank();
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
