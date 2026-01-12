package com.example.demo.community.comment.dto.response;

import java.util.List;

public record CommentPageResponse(
        List<CommentResponse> content,   // ✅ 이름 content로 프론트 구조 맞춤
        long totalCount,                 // ✅ 부모 + 자식 전체 댓글 수
        int totalPages,
        int pageNumber,
        int pageSize,
        boolean first,
        boolean last,
        int numberOfElements,
        boolean empty
) {
}
