package com.example.demo.attachment.dto.request;

public record AttachmentAnswerRequest(
        Long questionId,
        int score
) {
}
