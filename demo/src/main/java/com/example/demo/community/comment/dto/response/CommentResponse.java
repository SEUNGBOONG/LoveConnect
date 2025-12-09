package com.example.demo.community.comment.dto.response;

import com.example.demo.community.comment.domain.entity.Comment;

import java.util.List;

public record CommentResponse(
        Long commentId,
        Long postId,
        String content,
        Long writerId,
        String writerNickname,
        List<CommentResponse> children
) {
    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getPost().getId(),
                comment.getContent(),
                comment.getWriter().getId(),
                comment.getWriter().getMemberNickName(),
                comment.getChildren().stream()
                        .map(CommentResponse::from)
                        .toList()
        );
    }

    public static CommentResponse basic(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getPost().getId(),
                comment.getContent(),
                comment.getWriter().getId(),
                comment.getWriter().getMemberNickName(),
                null
        );
    }
}
