package com.example.demo.community.comment.dto.response;

import com.example.demo.community.comment.domain.entity.Comment;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record CommentResponse(
        Long commentId,
        Long writerId,
        String writerNickname,
        String content,
        boolean isMine,
        LocalDateTime createdAt,
        List<CommentResponse> children
) {

    public static CommentResponse from(Comment comment, Long myId) {
        return CommentResponse.builder()
                .commentId(comment.getId())
                .writerId(comment.getWriter().getId())
                .writerNickname(comment.getWriter().getMemberNickName())
                .content(comment.getContent())
                .isMine(comment.getWriter().getId().equals(myId))
                .createdAt(comment.getCreatedAt())
                .children(
                        comment.getChildren() == null
                                ? List.of()
                                : comment.getChildren().stream()
                                .map(child -> from(child, myId))
                                .toList()
                )
                .build();
    }

    // 댓글 생성/수정 직후 응답용
    public static CommentResponse basic(Comment comment, Long myId) {
        return from(comment, myId);
    }
}
