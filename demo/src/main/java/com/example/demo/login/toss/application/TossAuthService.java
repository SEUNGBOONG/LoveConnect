package com.example.demo.login.toss.application;

import com.example.demo.common.util.AESUtil;
import com.example.demo.config.toss.TossDecryptor;
import com.example.demo.login.member.domain.member.Member;
import com.example.demo.login.member.infrastructure.auth.JwtTokenProvider;
import com.example.demo.login.member.infrastructure.member.MemberJpaRepository;
import com.example.demo.login.toss.dto.request.TossAdditionalInfoRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TossAuthService {

    private final RestTemplate tossRestTemplate;
    private final MemberJpaRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider; // 토큰 발급기 주입 확인!

    @Value("${toss.api.base-url}")
    private String baseUrl;

    @Value("${toss.decrypt.key}")
    private String decryptKey;

    @Value("${toss.decrypt.aad}")
    private String decryptAad;

    @Transactional
    public Map<String, Object> executeTossLogin(String authCode) throws Exception {
        // [A] 토스 토큰 발급
        String tokenUrl = baseUrl + "/api-partner/v1/apps-in-toss/user/oauth2/generate-token";
        Map<String, String> tokenRequest = Map.of("authorizationCode", authCode, "referrer", "DEFAULT");
        Map response = tossRestTemplate.postForObject(tokenUrl, tokenRequest, Map.class);
        Map successData = (Map) response.get("success");
        String accessToken = (String) successData.get("accessToken");

        // [B] 토스 사용자 정보 획득
        String infoUrl = baseUrl + "/api-partner/v1/apps-in-toss/user/oauth2/login-me";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        ResponseEntity<Map> infoResponse = tossRestTemplate.exchange(infoUrl, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        Map userData = (Map) infoResponse.getBody().get("success");

        // [C] 데이터 복호화
        String name = TossDecryptor.decrypt((String) userData.get("name"), decryptKey, decryptAad);
        String phone = TossDecryptor.decrypt((String) userData.get("phone"), decryptKey, decryptAad);
        String cleanPhone = phone.replaceAll("[^0-9]", "");
        String encryptedPhone = AESUtil.encrypt(cleanPhone);
        // 🔥 Toss CI 복호화 (Disconnect 콜백용 핵심 값)
        String ci = TossDecryptor.decrypt(
                (String) userData.get("ci"),
                decryptKey,
                decryptAad
        );

        // [D] 기존 회원 조회 및 가입 처리
        Optional<Member> memberOpt = memberRepository.findByPhoneNumber(encryptedPhone);
        boolean isNewMember = memberOpt.isEmpty();

        Member member = memberOpt.orElseGet(() -> memberRepository.save(
                Member.builder()
                        .memberName(name)
                        .phoneNumber(encryptedPhone)
                        .memberEmail(cleanPhone + "@toss.user")
                        .memberNickName("토스_" + UUID.randomUUID().toString().substring(0, 5))
                        .memberPassword(UUID.randomUUID().toString())
                        .gender((String) userData.get("gender"))
                        .birthDate((String) userData.get("birthday"))
                        .instagramId(null) // 엔티티에서 nullable=true로 바꿨으니 null 가능
                        .mbti(null)
                        .emailAgree(true)
                        .privacyAgree(true)
                        .useAgree(true)
                        .build()
        ));
        // 🔥 Toss CI 저장 (신규/기존 회원 공통)
        member.setTossCi(ci);

        // [E] 결과 반환
        String jwtToken = jwtTokenProvider.createToken(member.getId());
        return Map.of(
                "token", jwtToken,
                "isNewMember", isNewMember,
                "memberId", member.getId(),
                "nickname", member.getMemberNickName()
        );
    }
    // TossAuthController 내부에 추가
// TossAuthService 내부에 추가
    @Transactional
    public void updateMemberProfile(Long memberId, TossAdditionalInfoRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("해당 회원을 찾을 수 없습니다."));

        // 기존에 엔티티에 만드신 updateProfile 메서드 활용
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
        Member member = memberRepository.findByTossCi(ci)
                .orElseThrow(() -> new RuntimeException("회원 없음"));

        member.disconnectToss(); // tossCi null 처리 or 상태 변경
    }

}
