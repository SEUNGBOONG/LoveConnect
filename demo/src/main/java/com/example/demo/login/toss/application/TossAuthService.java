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
    // [수정] throws Exception 추가하여 복호화 시 발생하는 예외를 상위로 던집니다.
    public Map<String, Object> executeTossLogin(String authorizationCode, String referrer) throws Exception {
        String tokenUrl = baseUrl + "/api-partner/v1/apps-in-toss/user/oauth2/generate-token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> body = Map.of("authorizationCode", authorizationCode, "referrer", referrer);

        ResponseEntity<Map> tokenResponse = tossRestTemplate.postForEntity(
                tokenUrl, new HttpEntity<>(body, headers), Map.class);

        Map tokenBody = tokenResponse.getBody();
        if (tokenBody == null || !(tokenBody.get("success") instanceof Map success)) {
            log.error("[TOSS] token issue failed: {}", tokenBody);
            throw new IllegalStateException("토스 토큰 발급 실패");
        }

        String accessToken = (String) success.get("accessToken");

        String infoUrl = baseUrl + "/api-partner/v1/apps-in-toss/user/oauth2/login-me";

        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(accessToken);

        ResponseEntity<Map> infoResponse = tossRestTemplate.exchange(
                infoUrl, HttpMethod.GET, new HttpEntity<>(authHeaders), Map.class);

        Map infoBody = infoResponse.getBody();
        if (infoBody == null || !(infoBody.get("success") instanceof Map user)) {
            log.error("[TOSS] user info failed: {}", infoBody);
            throw new IllegalStateException("토스 사용자 정보 조회 실패");
        }

        String name = (String) user.get("name");

        // [컴파일 에러 해결 지점]
        String decryptedPhone = TossDecryptor.decrypt((String) user.get("phone"), decryptKey, decryptAad);
        String ci = TossDecryptor.decrypt((String) user.get("ci"), decryptKey, decryptAad);
        Long userKey = ((Number) user.get("userKey")).longValue();

        String cleanPhone = decryptedPhone.replaceAll("[^0-9]", "");
        String encryptedPhone = AESUtil.encrypt(cleanPhone);

        Optional<Member> optional = memberRepository.findByPhoneNumber(encryptedPhone);
        boolean isNewMember = optional.isEmpty();

        // ... 기존 코드 (name, phone 복호화 부분 아래에 추가)

// 1. 토스 응답에서 암호화된 birthday 꺼내기
        String encryptedBirthday = (String) user.get("birthday");

// 2. 복호화 하기
        String birthday = TossDecryptor.decrypt(encryptedBirthday, decryptKey, decryptAad);
        String encryptedGender = (String) user.get("gender");
        String gender = TossDecryptor.decrypt(encryptedGender, decryptKey, decryptAad);
// 3. Member 저장 시 빌더에 추가
        Member member = optional.orElseGet(() -> memberRepository.save(
                Member.builder()
                        .memberName(name)
                        .phoneNumber(encryptedPhone)
                        .birthDate(birthday)
                        .gender(gender) // 👈 추가
                        .emailAgree(true) // 👈 추가
                        .privacyAgree(true) // 👈 추가
                        .useAgree(true) // 👈 추가
                        .memberEmail(cleanPhone + "@toss.user")
                        .memberNickName("토스_" + UUID.randomUUID().toString().substring(0, 6))
                        .memberPassword(UUID.randomUUID().toString())
                        .build()
        ));

        member.setTossCi(ci);
        member.setUserKey(userKey);

        String jwt = jwtTokenProvider.createToken(member.getId());

        return Map.of(
                "token", jwt,
                "isNewMember", isNewMember,
                "memberId", member.getId(),
                "nickname", member.getMemberNickName()
        );
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
