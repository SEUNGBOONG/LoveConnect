package com.example.demo.community.comment.domain.repository;

import com.example.demo.community.comment.domain.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long>, CommentQueryRepository {

    // 부모 댓글 페이징 조회
    Page<Comment> findAllByPostIdAndParentIsNull(Long postId, Pageable pageable);

    // 대댓글 조회 (자식 댓글)
    List<Comment> findAllByParentId(Long parentId);
}
