package com.example.demo.attachment.dto.request;

import java.util.List;

public record AttachmentSubmitRequest(
        List<AttachmentAnswerRequest> answers
) {}
