package com.example.demo.community.comment.dto.response;

import java.util.List;

public record CommentPageResponse(
        List<CommentResponse> comments,

        long totalCount,        // 전체 댓글 수 (부모+자식)
        int totalPages,
        int pageNumber,
        int pageSize,
        boolean first,
        boolean last,
        int numberOfElements
) {
}
