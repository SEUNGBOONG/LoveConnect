package com.example.demo.login.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
@RequiredArgsConstructor
@Slf4j
@Component
public class AligoSmsUtil {

    private final RestTemplate restTemplate;

    @Value("${aligo.api.key}")
    private String apiKey;

    @Value("${aligo.api.user_id}")
    private String userId;

    @Value("${aligo.api.sender}")
    private String sender;

    @Value("${aligo.api.url}")
    private String apiUrl;

    public void sendSms(String receiver, String message) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("key", apiKey);
        params.add("user_id", userId);
        params.add("sender", sender);
        params.add("receiver", receiver);
        params.add("msg", message);
        params.add("title", "LoveConnect 인증");

        try {
            String response = restTemplate.postForObject(apiUrl, params, String.class);
            log.info("✅ [알리고 SMS 전송 완료] 응답: {}", response);
        } catch (Exception e) {
            log.error("❌ [알리고 SMS 전송 실패] 에러: {}", e.getMessage(), e);
        }
    }
}
