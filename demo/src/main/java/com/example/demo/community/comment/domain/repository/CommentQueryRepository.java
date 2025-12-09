package com.example.demo.community.comment.domain.repository;

import com.example.demo.community.comment.domain.entity.Comment;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface CommentQueryRepository {

    List<Comment> findAllByPostWithWriter(Long postId); // 전체 fetch join

    Optional<Comment> findByIdWithWriter(Long commentId); // 수정/삭제시 사용

}
