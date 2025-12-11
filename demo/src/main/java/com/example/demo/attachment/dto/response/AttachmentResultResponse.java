package com.example.demo.attachment.dto.response;

public record AttachmentResultResponse(
        String resultType, // "안정형", "불안형", "회피형", "공포회피형"
        double anxiousScore,
        double avoidantScore
) {}
