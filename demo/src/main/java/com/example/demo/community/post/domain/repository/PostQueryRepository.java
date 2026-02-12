package com.example.demo.community.post.domain.repository;

import com.example.demo.community.post.domain.entity.Post;
import com.example.demo.community.post.dto.request.PostSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface PostQueryRepository {
    Optional<Post> findWithWriterById(Long postId);
    // ✅ findAllWithWriters() 제거 (PostController.getAll() 제거에 따라 사용처 없음)
    /** ✅ 게시글 검색 메서드 추가 */
    Page<Post> findAllWithWriter(Pageable pageable);
    Page<Post> searchPosts(PostSearchCondition condition, Pageable pageable);
}
