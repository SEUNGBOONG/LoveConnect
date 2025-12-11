package com.example.demo.chatgpt.service;

import com.example.demo.chatgpt.dto.ChatMessage;
import com.example.demo.chatgpt.dto.GPTRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GPTService {

    @Value("${OPEN_API_KEY}")
    private String apiKey;  // 🔥 application.properties 에서 불러오기

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String ask(String userMessage) throws IOException {

        OkHttpClient client = new OkHttpClient();

        // GPT 요청 구조 JSON 생성
        String jsonRequest = objectMapper.writeValueAsString(
                new GPTRequest(
                        "gpt-3.5-turbo",
                        List.of(
                                new ChatMessage("system", "너는 재회와 이별 상담 전문가다. 사용자의 고민을 분석해서 진심 어린 조언을 제공해라."),
                                new ChatMessage("user", userMessage)
                        ),
                        0.7
                )
        );

        RequestBody body = RequestBody.create(
                jsonRequest,
                MediaType.get("application/json")
        );

        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new IOException("GPT API 호출 실패: " + response);
        }

        JsonNode root = objectMapper.readTree(response.body().string());

        return root
                .get("choices")
                .get(0)
                .get("message")
                .get("content")
                .asText();
    }
}
