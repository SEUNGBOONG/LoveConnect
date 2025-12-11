package com.example.demo.chatgpt.dto;

import java.util.List;

public record GPTRequest(
        String model,
        List<ChatMessage> messages,
        double temperature
) {}
