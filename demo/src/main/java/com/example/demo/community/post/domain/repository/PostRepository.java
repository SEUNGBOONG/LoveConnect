package com.example.demo.community.post.domain.repository;

import com.example.demo.community.post.domain.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.awt.print.Pageable;

public interface PostRepository extends JpaRepository<Post, Long>, PostQueryRepository {
    @EntityGraph(attributePaths = "writer")
    Page<Post> findAll(Pageable pageable);
}
