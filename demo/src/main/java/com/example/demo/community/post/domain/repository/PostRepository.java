package com.example.demo.community.post.domain.repository;

import com.example.demo.community.post.domain.entity.Post;
import com.example.demo.login.member.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PostRepository extends JpaRepository<Post, Long>, PostQueryRepository {
    void deleteAllByWriter(Member writer);
}
