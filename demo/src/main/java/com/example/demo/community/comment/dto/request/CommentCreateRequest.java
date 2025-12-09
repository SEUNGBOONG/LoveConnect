package com.example.demo.community.comment.dto.request;

public record CommentCreateRequest(
        Long postId,
        String content,
        Long parentId // nullable
) { }
