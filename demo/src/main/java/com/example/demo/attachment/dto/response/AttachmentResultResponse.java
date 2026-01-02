package com.example.demo.attachment.dto.response;

public record AttachmentResultResponse(
        String resultType,
        double anxiousScore,
        double avoidantScore,
        String resultDescription
) {}
