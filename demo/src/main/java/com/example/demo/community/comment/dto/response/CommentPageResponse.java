package com.example.demo.community.comment.dto.response;

import java.util.List;

public record CommentPageResponse(
        List<CommentResponse> comments,
        long totalCount
) {}
