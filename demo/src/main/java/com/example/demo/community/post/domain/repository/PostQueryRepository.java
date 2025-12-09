package com.example.demo.community.post.domain.repository;

import com.example.demo.community.post.domain.entity.Post;

import java.util.List;
import java.util.Optional;

public interface PostQueryRepository {
    Optional<Post> findWithWriterById(Long postId);
    List<Post> findAllWithWriters();
}
