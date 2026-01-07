package com.example.demo.community.comment.domain.repository;

import com.example.demo.community.comment.domain.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CommentQueryRepository {

    // ✅ 기존 전체조회 메서드 제거 또는 사용 지양 (프론트에서 페이징 API 사용 권장)
    // List<Comment> findAllByPostWithWriter(Long postId);

    Optional<Comment> findByIdWithWriter(Long commentId); // 수정/삭제시 사용

    /** ✅ N+1 문제 해결을 위한 최적화된 페이징 조회 추가 */
    Page<Comment> findParentCommentsWithWritersAndChildren(Long postId, Pageable pageable);
}
