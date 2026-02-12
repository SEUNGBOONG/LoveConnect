package com.example.demo.community.post.domain.repository;

import com.example.demo.community.post.domain.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long>, PostQueryRepository {
    // 이거 지우고 직접 구현한 메서드 사용
}
