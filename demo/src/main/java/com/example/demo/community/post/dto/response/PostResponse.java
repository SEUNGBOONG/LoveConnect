package com.example.demo.community.post.dto.response;

import java.time.LocalDateTime;

public record PostResponse(
        Long id,
        String title,
        String content,
        String writerNickname,
        boolean isMine,          // ✅ 추가
        LocalDateTime createdAt  // ✅ 추가
) {
}
